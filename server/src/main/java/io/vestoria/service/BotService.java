package io.vestoria.service;

import io.vestoria.entity.BuildingEntity;
import io.vestoria.entity.ItemEntity;
import io.vestoria.entity.TransactionEntity;
import io.vestoria.entity.UserEntity;
import io.vestoria.enums.BuildingType;
import io.vestoria.enums.TransactionType;
import io.vestoria.repository.BuildingRepository;
import io.vestoria.repository.ItemRepository;
import io.vestoria.repository.TransactionRepository;
import io.vestoria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

  @Scheduled(fixedRate = 600000) // Every 10 minutes
  @Transactional
  public void buyFromShops() {
    List<BuildingEntity> shops = buildingRepository.findAll().stream()
        .filter(b -> b.getType() == BuildingType.SHOP)
        .toList();

    for (BuildingEntity shop : shops) {
      List<ItemEntity> itemsForSale = shop.getItems(); // Items in shop inventory are considered "for sale" to the bot

      for (ItemEntity item : itemsForSale) {
        if (item.getQuantity() > 0) {
          int quantityToBuy = item.getQuantity(); // Bot buys everything
          BigDecimal pricePerUnit = item.getPrice(); // Use item's base price or some logic
          // Dynamic pricing logic could go here. For now, use item price.

          BigDecimal totalEarnings = pricePerUnit.multiply(BigDecimal.valueOf(quantityToBuy));

          // Update Shop Owner Balance
          UserEntity owner = shop.getOwner();
          owner.setBalance(owner.getBalance().add(totalEarnings));

          // Update XP (Progression)
          owner.setXp(owner.getXp() + (quantityToBuy * 10L)); // 10 XP per item
          // Level up logic check could be here

          userRepository.save(owner);

          // Remove items
          item.setQuantity(0);
          itemRepository.save(item);

          // Record Transaction
          TransactionEntity transaction = TransactionEntity.builder()
              .type(TransactionType.SYSTEM_SELL)
              .buyer(null) // System is buyer
              .marketItem(null) // Direct sale, not via MarketEntity
              .price(totalEarnings)
              .amount(quantityToBuy)
              .build();
          @SuppressWarnings({ "null", "unused" })
          TransactionEntity saved = transactionRepository.save(transaction);

          // Create Notification
          String notificationMessage = String.format("%s %s dükkanından %d adet %s satın alındı. Kazanç: %s",
              owner.getUsername(), shop.getSubType(), quantityToBuy, item.getName(), totalEarnings);
          notificationService.createNotification(owner, notificationMessage);
        }
      }
    }
  }
}
