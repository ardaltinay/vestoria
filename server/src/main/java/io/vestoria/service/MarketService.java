package io.vestoria.service;

import io.vestoria.dto.request.BuyItemRequestDto;
import io.vestoria.dto.request.ListItemRequestDto;
import io.vestoria.entity.ItemEntity;
import io.vestoria.entity.MarketEntity;
import io.vestoria.entity.TransactionEntity;
import io.vestoria.entity.UserEntity;
import io.vestoria.entity.BuildingEntity;
import io.vestoria.enums.TransactionType;
import io.vestoria.enums.BuildingType;
import io.vestoria.enums.ItemCategory;
import io.vestoria.enums.BuildingSubType;
import io.vestoria.repository.ItemRepository;
import io.vestoria.repository.MarketRepository;
import io.vestoria.repository.TransactionRepository;
import io.vestoria.repository.UserRepository;
import io.vestoria.repository.BuildingRepository;
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
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MarketService {

  private final MarketRepository marketRepository;
  private final ItemRepository itemRepository;
  private final UserRepository userRepository;
  private final TransactionRepository transactionRepository;
  private final BuildingRepository buildingRepository;
  private final NotificationService notificationService;
  private final MarketConverter marketConverter;

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
    buyItem(user.getId(), marketItemId, request);
  }

  @Transactional
  @SuppressWarnings("null")
  @CacheEvict(value = { "globalSupply", "globalDemand", "activeListings" }, allEntries = true)
  public void buyItem(UUID userId, UUID marketItemId, BuyItemRequestDto request) {
    UserEntity buyer = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Alıcı bulunamadı"));

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
      if (marketItem.getQuantity() == 0) {
        marketItem.setIsActive(false);
      }
      marketRepository.save(marketItem);
    } catch (ObjectOptimisticLockingFailureException e) {
      throw new BusinessRuleException(
          "Bu ürün az önce başkası tarafından satın alındı veya güncellendi.");
    }

    // Find a suitable building for the buyer to store the item
    List<BuildingEntity> buyerBuildings = buildingRepository.findByOwnerId(buyer.getId());
    if (buyerBuildings.isEmpty()) {
      throw new BusinessRuleException(
          "Satın alınan ürünleri depolamak için en az bir işletmeye sahip olmalısınız.");
    }

    // Determine preferred building type based on item category
    BuildingType preferredType = BuildingType.SHOP;
    if (marketItem.getItem().getCategory() == ItemCategory.RAW_MATERIAL ||
        marketItem.getItem().getCategory() == ItemCategory.INDUSTRIAL) {
      preferredType = BuildingType.FACTORY;
    }

    // Try to find a preferred building first
    final BuildingType targetType = preferredType;
    BuildingEntity targetBuilding = buyerBuildings.stream()
        .filter(b -> b.getType() == targetType)
        .findFirst()
        .orElse(null);

    // If no preferred building found, or if we just need *any* compatible building
    if (targetBuilding == null) {
      // Fallback: Find ANY building that is compatible
      targetBuilding = buyerBuildings.stream()
          .filter(b -> isCompatible(b, marketItem.getItem().getCategory()))
          .findFirst()
          .orElseThrow(() -> new BusinessRuleException(
              "Bu ürünü depolayabilecek uygun bir işletmeniz (Örn: " + targetType + ") bulunamadı."));
    }

    // Check Compatibility
    if (!isCompatible(targetBuilding, marketItem.getItem().getCategory())) {
      throw new BusinessRuleException(
          "Bu ürün bu işletme türü için uygun değil.");
    }

    // Check if buyer already has this item in the target building
    // The `item` variable here refers to the original item from the market listing.
    // We need to use `marketItem.getItem().getName()` for the name.
    Optional<ItemEntity> existingItemOpt = itemRepository.findByBuildingIdAndName(targetBuilding.getId(),
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
      // Check Shop Capacity Limit
      // Count distinct item types currently in stock (quantity > 0)
      long activeItemCount = itemRepository.countByBuildingIdAndQuantityGreaterThan(targetBuilding.getId(), 0);

      // Capacity is equal to building level
      if (activeItemCount >= targetBuilding.getLevel()) {
        throw new BusinessRuleException(
            "Dükkan kapasitesi dolu! Seviye " + targetBuilding.getLevel() + " dükkan en fazla "
                + targetBuilding.getLevel() + " çeşit ürün satabilir.");
      }

      // Create new item for buyer
      ItemEntity newItem = ItemEntity.builder()
          .name(marketItem.getItem().getName())
          .unit(marketItem.getItem().getUnit())
          .price(marketItem.getItem().getPrice()) // Base price of the item, not market price
          .quantity(quantity)
          .qualityScore(marketItem.getItem().getQualityScore())
          .tier(marketItem.getItem().getTier())
          .category(marketItem.getItem().getCategory()) // Ensure category is copied
          .building(targetBuilding)
          // Supply/Demand are global or not set on individual items usually, but we copy
          // if needed
          .supply(marketItem.getItem().getSupply())
          .demand(marketItem.getItem().getDemand())
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

  private boolean isCompatible(BuildingEntity building, ItemCategory category) {
    if (category == null)
      return true; // Legacy items support

    // Factories can store Raw Materials and Industrial items
    if (building.getType() == BuildingType.FACTORY) {
      return category == ItemCategory.RAW_MATERIAL ||
          category == ItemCategory.INDUSTRIAL;
    }

    // Shops have specific categories
    if (building.getType() == BuildingType.SHOP) {
      BuildingSubType subType = building.getSubType();
      if (subType == null)
        return true;

      switch (subType) {
        case MARKET:
          return category == ItemCategory.FOOD;
        case CLOTHING:
          return category == ItemCategory.CLOTHING;
        case GREENGROCER:
          return category == ItemCategory.FRESH_PRODUCE;
        case JEWELER:
          return category == ItemCategory.JEWELRY;
        default:
          return true;
      }
    }

    // Other buildings (Farms, Mines) usually produce, but if they need to buy
    // seeds/equipment:
    // For now, let's assume they don't buy items from the market to store,
    // or we can allow RAW_MATERIAL for them too if needed.
    // Let's allow them to store anything for now to avoid blocking game flow until
    // we have specific categories for them.
    return true;
  }

  @Cacheable("globalSupply")
  public long calculateGlobalSupply(String itemName) {
    Long supply = itemRepository.sumSupplyByItemNameAndBuildingType(itemName, BuildingType.SHOP);
    return supply != null ? supply : 0L;
  }

  @Cacheable("globalDemand")
  public long calculateGlobalDemand(String itemName) {
    // Sum of amounts in transactions for this item in the last 24 hours
    java.time.LocalDateTime twentyFourHoursAgo = java.time.LocalDateTime.now().minusHours(24);
    Integer demand = transactionRepository.sumAmountByItemNameAndCreatedAtAfter(itemName, twentyFourHoursAgo);
    return demand != null ? demand.longValue() : 0L;
  }
}
