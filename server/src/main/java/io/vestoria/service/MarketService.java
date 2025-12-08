package io.vestoria.service;

import io.vestoria.converter.MarketConverter;
import io.vestoria.dto.request.BuyItemRequestDto;
import io.vestoria.dto.request.ListItemRequestDto;
import io.vestoria.dto.response.MarketResponseDto;
import io.vestoria.dto.response.MarketStatsDto;
import io.vestoria.dto.response.MarketTrendDto;
import io.vestoria.dto.response.MarketUpdateDto;
import io.vestoria.dto.response.TrendingItemDto;
import io.vestoria.entity.ItemEntity;
import io.vestoria.entity.MarketEntity;
import io.vestoria.entity.TransactionEntity;
import io.vestoria.entity.UserEntity;
import io.vestoria.enums.TransactionType;
import io.vestoria.exception.BusinessRuleException;
import io.vestoria.exception.InsufficientBalanceException;
import io.vestoria.exception.ResourceNotFoundException;
import io.vestoria.exception.UnauthorizedAccessException;
import io.vestoria.repository.ItemRepository;
import io.vestoria.repository.MarketRepository;
import io.vestoria.repository.TransactionRepository;
import io.vestoria.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarketService {

    private final MarketRepository marketRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;
    private final MarketConverter marketConverter;
    private final SimpMessagingTemplate messagingTemplate;
    private final EconomicService economicService;

    public BigDecimal getEstimatedMarketPrice(String itemName) {
        return economicService.getMarketPrice(itemName);
    }

    @Transactional
    @CacheEvict(value = { "globalDemand", "globalSupply", "activeListings" }, allEntries = true)
    public MarketEntity listItem(UUID userId, UUID itemId, ListItemRequestDto request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        ItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Ürün bulunamadı"));

        if (!item.getOwner().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Bu ürün size ait değil");
        }

        if (item.getQuantity() < request.getQuantity()) {
            throw new BusinessRuleException("Yetersiz ürün miktarı");
        }

        // Deduct items from user inventory (or reserve them)
        // For simplicity, we decrease quantity immediately.
        // If cancelled, we add back.
        item.setQuantity(item.getQuantity() - request.getQuantity());
        itemRepository.save(item);

        // Check if there is an existing active listing for this user, item and price
        Optional<MarketEntity> existingListing = marketRepository.findBySellerAndItemNameAndPriceAndIsActiveTrue(user,
                item.getName(), request.getPrice());

        MarketEntity savedItem;
        if (existingListing.isPresent()) {
            // Merge with existing listing
            MarketEntity listing = existingListing.get();
            // Optional: Check quality tolerance. For now, we assume if price is same, user
            // intends to group them.
            // Or we can check if quality is exactly same.
            if (listing.getItem().getQualityScore().compareTo(item.getQualityScore()) == 0) {
                listing.setQuantity(listing.getQuantity() + request.getQuantity());
                savedItem = marketRepository.save(listing);
            } else {
                // Quality mismatch, create new listing
                MarketEntity marketItem = new MarketEntity();
                marketItem.setSeller(user);
                marketItem.setItem(item);
                marketItem.setPrice(request.getPrice());
                marketItem.setQuantity(request.getQuantity());
                marketItem.setIsActive(true);
                savedItem = marketRepository.save(marketItem);
            }
        } else {
            // Create New Market Listing
            MarketEntity marketItem = new MarketEntity();
            marketItem.setSeller(user);
            marketItem.setItem(item);
            marketItem.setPrice(request.getPrice());
            marketItem.setQuantity(request.getQuantity());
            marketItem.setIsActive(true);
            savedItem = marketRepository.save(marketItem);
        }

        // Publish WebSocket Event
        messagingTemplate.convertAndSend("/topic/market",
                MarketUpdateDto.builder().type("LIST").id(savedItem.getId()).itemName(savedItem.getItem().getName())
                        .quantity(savedItem.getQuantity()).price(savedItem.getPrice())
                        .sellerName(savedItem.getSeller().getUsername()).build());

        return savedItem;
    }

    @Transactional
    public MarketEntity listItem(String username, UUID itemId, ListItemRequestDto request) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + username));
        return listItem(user.getId(), itemId, request);
    }

    @Transactional
    public void buyItem(String username, UUID marketItemId, BuyItemRequestDto request) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + username));
        buyItem(user, marketItemId, request);
    }

    @Transactional
    @SuppressWarnings("null")
    @CacheEvict(value = { "globalDemand", "globalSupply", "activeListings" }, allEntries = true)
    public void buyItem(UserEntity buyer, UUID marketItemId, BuyItemRequestDto request) {
        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                MarketEntity marketItem = marketRepository.findById(marketItemId)
                        .orElseThrow(() -> new ResourceNotFoundException("Pazar ürünü bulunamadı"));

                if (marketItem.getSeller().getId().equals(buyer.getId())) {
                    throw new BusinessRuleException("Kendi ürününüzü satın alamazsınız");
                }

                if (!marketItem.getIsActive()) {
                    throw new BusinessRuleException("Bu ürün artık satışta değil");
                }

                if (marketItem.getQuantity() < request.getQuantity()) {
                    throw new BusinessRuleException("Pazarda yeterli miktarda ürün yok");
                }

                BigDecimal totalCost = marketItem.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

                if (buyer.getBalance().compareTo(totalCost) < 0) {
                    throw new InsufficientBalanceException(
                            "Yetersiz bakiye! Bu işlem için " + totalCost + " TL gerekiyor.");
                }

                // Process Transaction
                UserEntity seller = marketItem.getSeller();

                // Transfer Money
                buyer.setBalance(buyer.getBalance().subtract(totalCost));
                seller.setBalance(seller.getBalance().add(totalCost));

                userRepository.save(buyer);
                userRepository.save(seller);

                int quantity = request.getQuantity();

                // Update Market Item (Skip if seller is Vestoria)
                if (!"vestoria".equals(seller.getUsername())) {
                    marketItem.setQuantity(marketItem.getQuantity() - quantity);
                    if (marketItem.getQuantity() <= 0) {
                        marketItem.setIsActive(false);
                    }
                    marketRepository.save(marketItem);
                }

                // Publish WebSocket Event
                messagingTemplate.convertAndSend("/topic/market",
                        io.vestoria.dto.response.MarketUpdateDto.builder().type("BUY").id(marketItem.getId())
                                .itemName(marketItem.getItem().getName()).quantity(quantity) // Amount bought
                                .price(marketItem.getPrice()).sellerName(seller.getUsername()).build());

                // Add item to buyer's centralized inventory (building = null)
                // Check if buyer already has this item in centralized inventory
                Optional<ItemEntity> existingItemOpt = itemRepository.findByBuildingIdAndName(null,
                        marketItem.getItem().getName());

                if (existingItemOpt.isPresent()) {
                    ItemEntity existingItem = existingItemOpt.get();

                    // Calculate weighted average quality
                    BigDecimal oldQuantity = BigDecimal.valueOf(existingItem.getQuantity());
                    BigDecimal newQuantity = BigDecimal.valueOf(quantity);
                    BigDecimal totalQuantity = oldQuantity.add(newQuantity);

                    BigDecimal oldTotalQuality = existingItem.getQualityScore().multiply(oldQuantity);
                    BigDecimal newTotalQuality = marketItem.getItem().getQualityScore().multiply(newQuantity);

                    BigDecimal newAvgQuality = oldTotalQuality.add(newTotalQuality).divide(totalQuantity, 2,
                            java.math.RoundingMode.HALF_UP);

                    existingItem.setQuantity(existingItem.getQuantity() + quantity);
                    existingItem.setQualityScore(newAvgQuality);
                    itemRepository.save(existingItem);
                } else {
                    // Create new item in centralized inventory
                    ItemEntity newItem = ItemEntity.builder().name(marketItem.getItem().getName())
                            .unit(marketItem.getItem().getUnit()).price(null) // Price is null until user sets it
                            .cost(marketItem.getPrice()) // Cost is what they paid
                            .quantity(quantity).qualityScore(marketItem.getItem().getQualityScore())
                            .tier(marketItem.getItem().getTier()).building(null) // Centralized inventory - no building
                            .owner(buyer) // Set owner
                            .build();
                    itemRepository.save(newItem);
                }

                // Record Transaction
                TransactionEntity transaction = TransactionEntity.builder().type(TransactionType.MARKET_BUY)
                        .buyer(buyer).seller(seller) // Added seller to transaction
                        .marketItem(marketItem).price(totalCost) // Changed to totalCost
                        .amount(quantity).itemName(marketItem.getItem().getName()).build();
                transactionRepository.save(transaction);

                // Create Notification for Seller
                String sourceInfo = "";
                if (marketItem.getItem().getBuilding() != null) {
                    sourceInfo = String.format(" (%s işletmesinden)", marketItem.getItem().getBuilding().getName());
                }

                String notificationMessage = String.format("%s kullanıcısı%s %d adet %s satın aldı. Kazanç: %s",
                        buyer.getUsername(), sourceInfo, quantity, marketItem.getItem().getName(), totalCost);
                notificationService.createNotification(seller, notificationMessage);

                break; // Success, exit loop

            } catch (ObjectOptimisticLockingFailureException e) {
                attempt++;
                if (attempt == maxRetries) {
                    throw new BusinessRuleException(
                            "Yoğunluk nedeniyle işlem gerçekleştirilemedi. Lütfen tekrar deneyiniz.");
                }
                // Retry loop continues
            }
        }
    }

    @Transactional
    @CacheEvict(value = { "globalDemand", "globalSupply", "activeListings" }, allEntries = true)
    public void cancelListing(String username, UUID listingId) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        MarketEntity listing = marketRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("İlan bulunamadı"));

        if (!listing.getSeller().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Bu ilanı iptal etme yetkiniz yok");
        }

        if (!listing.getIsActive()) {
            throw new BusinessRuleException("Bu ilan zaten aktif değil");
        }

        // Restore quantity to item
        ItemEntity item = listing.getItem();
        item.setQuantity(item.getQuantity() + listing.getQuantity());
        itemRepository.save(item);

        // Deactivate listing
        listing.setIsActive(false);
        marketRepository.save(listing);

        // Publish WebSocket Event
        messagingTemplate.convertAndSend("/topic/market",
                MarketUpdateDto.builder()
                        .type("CANCEL")
                        .id(listing.getId())
                        // Other fields can be null for CANCEL
                        .build());
    }

    // @Cacheable("activeListings") - Disabled due to Redis serialization issues
    // with PageImpl
    public Page<MarketResponseDto> getActiveListings(String search, int page, int size) {
        // Note: We are not caching search results for now as they are dynamic
        String searchTerm = search != null ? "%" + search.toLowerCase() + "%" : null;
        return marketRepository.findAllActiveWithDetails(searchTerm, PageRequest.of(page, size))
                .map(marketConverter::toResponseDto);
    }

    @Cacheable("globalDemand")
    public long calculateGlobalDemand(String itemName) {
        // Sum of amounts in transactions for this item in the last 24 hours
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        Integer demand = transactionRepository.sumByItemNameAndCreatedAtAfter(itemName, twentyFourHoursAgo);
        return demand != null ? demand.longValue() : 0L;
    }

    @Cacheable("globalSupply")
    public long calculateGlobalSupply(String itemName) {
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        Integer supply = marketRepository.sumByItemNameAndCreatedAtAfter(itemName, twentyFourHoursAgo);
        return supply != null ? supply.longValue() : 0L;
    }

    @Cacheable("marketTrends")
    public List<MarketTrendDto> getMarketTrends() {
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        List<TrendingItemDto> topItems = transactionRepository.findTopTrendingItems(twentyFourHoursAgo,
                PageRequest.of(0, 5));

        if (topItems.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> itemNames = topItems.stream().map(TrendingItemDto::getItemName).collect(Collectors.toList());

        List<MarketStatsDto> statsList = marketRepository.findMarketStatsByItemNames(itemNames);
        Map<String, MarketStatsDto> statsMap = statsList.stream()
                .collect(Collectors.toMap(MarketStatsDto::getItemName, stats -> stats));

        return topItems.stream().map(item -> {
            String itemName = item.getItemName();
            Long demand = item.getTotalAmount();

            MarketStatsDto stats = statsMap.get(itemName);
            BigDecimal minPrice = (stats != null && stats.getMinPrice() != null)
                    ? stats.getMinPrice()
                    : BigDecimal.ZERO;
            long supply = (stats != null && stats.getActiveCount() != null) ? stats.getActiveCount() : 0L;

            // Calculate Trend
            String trend = "stable";
            double ratio = (double) demand / (supply + 1);
            if (ratio > 1.5)
                trend = "up";
            else if (ratio < 0.5)
                trend = "down";

            // Calculate Change (Mock based on ratio)
            double change = (ratio - 1) * 5.0;
            if (change < -10)
                change = -10;
            if (change > 10)
                change = 10;

            return MarketTrendDto.builder().name(itemName).price(minPrice).trend(trend).change(Math.abs(change))
                    .build();
        }).collect(Collectors.toList());
    }
}
