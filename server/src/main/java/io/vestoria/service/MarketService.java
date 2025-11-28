package io.vestoria.service;

import io.vestoria.dto.request.BuyItemRequestDto;
import io.vestoria.dto.request.ListItemRequestDto;
import io.vestoria.entity.ItemEntity;
import io.vestoria.entity.MarketEntity;
import io.vestoria.entity.TransactionEntity;
import io.vestoria.entity.UserEntity;
import io.vestoria.enums.TransactionType;
import io.vestoria.repository.ItemRepository;
import io.vestoria.repository.MarketRepository;
import io.vestoria.repository.TransactionRepository;
import io.vestoria.repository.UserRepository;
import io.vestoria.exception.ResourceNotFoundException;
import io.vestoria.exception.UnauthorizedAccessException;
import io.vestoria.exception.BusinessRuleException;
import io.vestoria.exception.InsufficientBalanceException;
import io.vestoria.converter.MarketConverter;
import io.vestoria.dto.response.MarketResponseDto;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MarketService {

  private final MarketRepository marketRepository;
  private final ItemRepository itemRepository;
  private final UserRepository userRepository;
  private final TransactionRepository transactionRepository;
  private final NotificationService notificationService;
  private final MarketConverter marketConverter;
  private final GameDataService gameDataService;

  @Transactional
  @SuppressWarnings("null")
  public MarketEntity listItem(UUID userId, UUID itemId, ListItemRequestDto request) {
    UserEntity user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

    ItemEntity item = itemRepository.findById(itemId)
        .orElseThrow(() -> new ResourceNotFoundException("Ürün bulunamadı"));

    if (!item.getBuilding().getOwner().getId().equals(userId)) {
      throw new UnauthorizedAccessException("Bu ürün size ait değil");
    }

    if (item.getQuantity() < request.getQuantity()) {
      throw new BusinessRuleException("Yetersiz ürün miktarı");
    }

    // Create Market Listing
    MarketEntity marketItem = new MarketEntity();
    marketItem.setSeller(user);
    marketItem.setItem(item);
    marketItem.setPrice(request.getPrice());
    marketItem.setQuantity(request.getQuantity());
    marketItem.setIsActive(true);

    // Deduct items from user inventory (or reserve them)
    // For simplicity, we decrease quantity immediately.
    // If cancelled, we add back.
    item.setQuantity(item.getQuantity() - request.getQuantity());
    itemRepository.save(item);

    return marketRepository.save(marketItem);
  }

  @Transactional
  @CacheEvict(value = "activeListings", allEntries = true)
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
  @CacheEvict(value = { "globalSupply", "globalDemand", "activeListings" }, allEntries = true)
  public void buyItem(UserEntity buyer, UUID marketItemId, BuyItemRequestDto request) {
    MarketEntity marketItem = marketRepository.findById(marketItemId)
        .orElseThrow(() -> new ResourceNotFoundException("Pazar ürünü bulunamadı"));

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

    try {
      // Update Market Item
      marketItem.setQuantity(marketItem.getQuantity() - quantity);
      if (marketItem.getQuantity() <= 0) {
        marketItem.setIsActive(false);
      }
      marketRepository.save(marketItem);
    } catch (ObjectOptimisticLockingFailureException e) {
      throw new BusinessRuleException(
          "Bu ürün az önce başkası tarafından satın alındı veya güncellendi. Biraz bekleyip yeniden deneyiniz.");
    }

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

      BigDecimal newAvgQuality = oldTotalQuality.add(newTotalQuality)
          .divide(totalQuantity, 2, java.math.RoundingMode.HALF_UP);

      existingItem.setQuantity(existingItem.getQuantity() + quantity);
      existingItem.setQualityScore(newAvgQuality);
      itemRepository.save(existingItem);
    } else {
      // Create new item in centralized inventory
      ItemEntity newItem = ItemEntity.builder()
          .name(marketItem.getItem().getName())
          .unit(marketItem.getItem().getUnit())
          .price(marketItem.getItem().getPrice())
          .quantity(quantity)
          .qualityScore(marketItem.getItem().getQualityScore())
          .tier(marketItem.getItem().getTier())
          .building(null) // Centralized inventory - no building
          .owner(buyer) // Set owner
          .build();
      itemRepository.save(newItem);
    }

    // Record Transaction
    TransactionEntity transaction = TransactionEntity.builder()
        .type(TransactionType.MARKET_BUY)
        .buyer(buyer)
        .seller(seller) // Added seller to transaction
        .marketItem(marketItem)
        .price(totalCost) // Changed to totalCost
        .amount(quantity)
        .itemName(marketItem.getItem().getName())
        .build();
    transactionRepository.save(transaction);

    // Create Notification for Seller
    String notificationMessage = String.format("%s kullanıcısı %d adet %s satın aldı. Kazanç: %s",
        buyer.getUsername(), quantity, marketItem.getItem().getName(), totalCost);
    notificationService.createNotification(seller, notificationMessage);
  }

  @Cacheable("activeListings")
  public Page<MarketResponseDto> getActiveListings(String search, int page, int size) {
    // Note: We are not caching search results for now as they are dynamic
    String searchTerm = search != null ? "%" + search.toLowerCase() + "%" : null;
    return marketRepository.findAllActiveWithDetails(searchTerm, PageRequest.of(page, size))
        .map(marketConverter::toResponseDto);
  }

  @Cacheable("globalSupply")
  public long calculateGlobalSupply(String itemName) {
    Long supply = itemRepository.sumQuantityByName(itemName);
    return supply != null ? supply : 0L;
  }

  @Cacheable("globalDemand")
  public long calculateGlobalDemand(String itemName) {
    // Sum of amounts in transactions for this item in the last 24 hours
    LocalDateTime twentyFourHoursAgo = java.time.LocalDateTime.now().minusHours(24);
    Integer demand = transactionRepository.sumAmountByItemNameAndCreatedAtAfter(itemName, twentyFourHoursAgo);
    return demand != null ? demand.longValue() : 0L;
  }
}
