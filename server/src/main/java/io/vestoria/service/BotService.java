package io.vestoria.service;

import io.vestoria.entity.BuildingEntity;
import io.vestoria.entity.ItemEntity;
import io.vestoria.entity.TransactionEntity;
import io.vestoria.entity.UserEntity;
import io.vestoria.enums.BuildingType;
import io.vestoria.enums.ItemTier;
import io.vestoria.enums.TransactionType;
import io.vestoria.exception.BusinessRuleException;
import io.vestoria.exception.ResourceNotFoundException;
import io.vestoria.repository.BuildingRepository;
import io.vestoria.repository.ItemRepository;
import io.vestoria.repository.TransactionRepository;
import io.vestoria.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // @Scheduled(fixedRate = 60000) // Every minute - DISABLED as per user request
    @Transactional
    public void checkExpiredSales() {
        List<BuildingEntity> expiredSales = buildingRepository
                .findAllByIsSellingTrueAndSalesEndsAtBefore(LocalDateTime.now());
        for (BuildingEntity shop : expiredSales) {
            processShopSales(shop.getId());
        }
    }

    @Transactional
    @CacheEvict(value = { "globalSupply", "globalDemand" }, allEntries = true)
    public void processShopSales(UUID buildingId) {
        BuildingEntity shop = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("Dükkan bulunamadı"));

        if (shop.getType() != BuildingType.SHOP) {
            throw new BusinessRuleException("Bu işletme türü satış yapmak için uygun değil!");
        }

        if (!shop.getIsSelling()) {
            throw new BusinessRuleException("Bu işletmede satışa çıkarılmış herhangi bir ürün yok!");
        }

        List<ItemEntity> itemsForSale = shop.getItems();
        BigDecimal totalBatchEarnings = BigDecimal.ZERO;

        for (ItemEntity item : itemsForSale) {
            if (item.getQuantity() > 0) {

                long globalDemand = marketService.calculateGlobalDemand(item.getName());
                long globalSupply = marketService.calculateGlobalSupply(item.getName());

                ItemTier tier = item.getTier();
                item.setTier(tier);

                double supplyVal = (double) globalSupply == 0L ? 1L : globalSupply;
                double demandVal = (double) globalDemand;
                double tierValue = tier.value; // 0.5, 1.0, 1.5, 2.0

                double rawRatio = demandVal / supplyVal;
                double baseScore = rawRatio * tierValue;

                double finalScore = Math.min(baseScore, 100.0);

                double buyPercentage = finalScore / 100.0;

                // Luck factor for sales (Market Volatility): 0.8 to 1.2 (+/- 20%)
                double salesLuck = 0.8 + (Math.random() * 0.4);
                buyPercentage = buyPercentage * salesLuck;

                // Level factor for sales: +1% per level
                int userLevel = shop.getOwner().getLevel() != null ? shop.getOwner().getLevel() : 1;
                double levelMultiplier = 1.0 + (userLevel * 0.01);
                buyPercentage = buyPercentage * levelMultiplier;

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
                    totalBatchEarnings = totalBatchEarnings.add(totalEarnings);

                    UserEntity owner = shop.getOwner();
                    owner.setBalance(owner.getBalance().add(totalEarnings));
                    userRepository.save(owner);

                    // Update XP (Progression) via UserService
                    userService.addXp(owner, quantityToBuy * 10L); // 10 XP per item

                    // Remove items
                    item.setQuantity(item.getQuantity() - quantityToBuy);
                    itemRepository.save(item);

                    // Fetch Bot User for System Buys
                    UserEntity botUser = userRepository.findByUsername("VESTORIA")
                            .orElseThrow(() -> new ResourceNotFoundException("vestoria user not found"));

                    // Record Transaction
                    TransactionEntity transaction = TransactionEntity.builder().type(TransactionType.SYSTEM_SELL)
                            .buyer(botUser) // System (Bot) is buyer
                            .seller(owner).marketItem(null) // Direct sale
                            .price(totalEarnings).amount(quantityToBuy).itemName(item.getName()) // Store item name
                            .build();
                    transactionRepository.save(transaction);

                    // Create Notification
                    String notificationMessage = String.format("%s %s dükkanından %d adet %s satın alındı. Kazanç: %s",
                            owner.getUsername(), shop.getSubType(), quantityToBuy, item.getName(), totalEarnings,
                            finalScore);
                    notificationService.createNotification(owner, notificationMessage);
                } else {
                    // Just save the score if we didn't buy anything
                    itemRepository.save(item);
                }
            }
        }

        // Reset shop status
        shop.setIsSelling(false);
        shop.setSalesEndsAt(null);
        shop.setLastRevenue(totalBatchEarnings);
        buildingRepository.save(shop);
    }
}
