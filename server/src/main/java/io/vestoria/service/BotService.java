package io.vestoria.service;

import io.vestoria.entity.BuildingEntity;
import io.vestoria.entity.ItemEntity;
import io.vestoria.entity.TransactionEntity;
import io.vestoria.entity.UserEntity;
import io.vestoria.enums.BuildingType;
import io.vestoria.enums.ItemCategory;
import io.vestoria.enums.ItemTier;
import io.vestoria.enums.TransactionType;
import io.vestoria.repository.BuildingRepository;
import io.vestoria.repository.ItemRepository;
import io.vestoria.repository.TransactionRepository;
import io.vestoria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BotService {

  private final BuildingRepository buildingRepository;
  private final ItemRepository itemRepository;
  private final UserRepository userRepository;
  private final TransactionRepository transactionRepository;
  private final NotificationService notificationService;
  private final UserService userService;
  private final MarketService marketService;

  @Scheduled(fixedRate = 600000) // Every 10 minutes
  @Transactional
  @CacheEvict(value = { "globalSupply", "globalDemand" }, allEntries = true)
  public void buyFromShops() {
    List<BuildingEntity> shops = buildingRepository.findAll().stream()
        .filter(b -> b.getType() == BuildingType.SHOP)
        .toList();

    for (BuildingEntity shop : shops) {
      List<ItemEntity> itemsForSale = shop.getItems();

      for (ItemEntity item : itemsForSale) {
        if (item.getQuantity() > 0) {

          long globalSupply = marketService.calculateGlobalSupply(item.getName());

          long globalDemand = marketService.calculateGlobalDemand(item.getName());

          ItemTier tier = determineTier(item);
          item.setTier(tier);

          item.setSupply(globalSupply);
          item.setDemand(globalDemand);

          double supplyVal = Math.max(globalSupply, 1.0);
          double demandVal = (double) globalDemand;
          double tierValue = tier.value; // 0.5, 1.0, 1.5, 2.0

          double rawRatio = demandVal / supplyVal;
          double baseScore = rawRatio * tierValue;

          double finalScore = Math.min(baseScore, 100.0);

          BigDecimal qualityScore = BigDecimal.valueOf(finalScore);
          item.setQualityScore(qualityScore);

          double buyPercentage = finalScore / 100.0;

          int quantityToBuy = (int) Math.ceil(item.getQuantity() * buyPercentage);

          if (quantityToBuy == 0 && buyPercentage > 0.01) {
            quantityToBuy = 1;
          }

          if (quantityToBuy > item.getQuantity()) {
            quantityToBuy = item.getQuantity();
          }

          if (quantityToBuy > 0) {
            BigDecimal pricePerUnit = item.getPrice();
            BigDecimal totalEarnings = pricePerUnit.multiply(BigDecimal.valueOf(quantityToBuy));

            UserEntity owner = shop.getOwner();
            owner.setBalance(owner.getBalance().add(totalEarnings));
            userRepository.save(owner);

            // Update XP (Progression) via UserService
            userService.addXp(owner, quantityToBuy * 10L); // 10 XP per item

            // Remove items
            item.setQuantity(item.getQuantity() - quantityToBuy);
            itemRepository.save(item);

            // Record Transaction
            TransactionEntity transaction = TransactionEntity.builder()
                .type(TransactionType.SYSTEM_SELL)
                .buyer(null) // System is buyer
                .seller(owner)
                .marketItem(null) // Direct sale
                .price(totalEarnings)
                .amount(quantityToBuy)
                .itemName(item.getName()) // Store item name
                .build();
            @SuppressWarnings({ "null", "unused" })
            TransactionEntity saved = transactionRepository.save(transaction);

            // Create Notification
            String notificationMessage = String.format(
                "%s %s dükkanından %d adet %s satın alındı. Kazanç: %s (Puan: %.2f)",
                owner.getUsername(), shop.getSubType(), quantityToBuy, item.getName(), totalEarnings, finalScore);
            notificationService.createNotification(owner, notificationMessage);
          } else {
            // Just save the score if we didn't buy anything
            itemRepository.save(item);
          }
        }
      }
    }
  }

  private ItemTier determineTier(ItemEntity item) {
    // Logic based on Category
    ItemCategory category = item.getCategory();
    if (category == null)
      return ItemTier.LOW;

    switch (category) {
      case RAW_MATERIAL:
      case FRESH_PRODUCE:
        return ItemTier.LOW;
      case FOOD:
      case CLOTHING: // Intermediate/Processed
        return ItemTier.MEDIUM;
      case INDUSTRIAL:
      case JEWELRY: // High value
        return ItemTier.SCARCE;
      default:
        return ItemTier.LOW;
    }
  }
}
