package io.vestoria.service;

import io.vestoria.constant.Constants;
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
    private final EconomicService economicService;

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
    @CacheEvict(value = { "globalSupply", "globalDemand", "getUserBuildings" }, allEntries = true)
    public void processShopSales(UUID buildingId) {
        BuildingEntity shop = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("Dükkan bulunamadı"));

        if (shop.getType() != BuildingType.SHOP) {
            throw new BusinessRuleException("Bu işletme türü satış yapmak için uygun değil!");
        }

        if (!Boolean.TRUE.equals(shop.getIsSelling())) {
            throw new BusinessRuleException("Bu işletmede satışa çıkarılmış herhangi bir ürün yok!");
        }

        List<ItemEntity> itemsForSale = shop.getItems();
        BigDecimal totalBatchEarnings = BigDecimal.ZERO;
        StringBuilder salesSummary = new StringBuilder();

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

                // Price Factor Calculation
                BigDecimal salesPrice = item.getPrice(); // User set price

                // Get Dynamic Market Price for Reference
                BigDecimal marketPrice = economicService.getMarketPrice(item.getName());

                double priceMultiplier = 1.0;
                if (marketPrice.compareTo(BigDecimal.ZERO) > 0) {
                    // Compare user price against Real Time Market Price
                    double ratio = salesPrice.doubleValue() / marketPrice.doubleValue();
                    if (ratio <= 1.0) {
                        // Cheaper than market average: Boost sales significantly
                        priceMultiplier = 1.0 + (1.0 - ratio) * 3; // Aggressive boost if cheap
                    } else {
                        // More expensive: Reduce sales slightly
                        // Logic: Users might still buy if convenient, but less likely
                        priceMultiplier = 1.0 / (ratio * ratio);
                    }
                }
                buyPercentage = buyPercentage * priceMultiplier;

                int quantityToBuy = (int) Math.ceil(item.getQuantity() * buyPercentage);

                if (quantityToBuy == 0 && buyPercentage > 0.01) {
                    quantityToBuy = 1;
                }

                // Guarantee at least 1 sale if price is not invalid (extremely high)
                // If price is less than 3x base price, ensure at least 1 item is sold 50% of
                // the time (random chance)
                if (quantityToBuy == 0 && priceMultiplier > 0.1) {
                    if (Math.random() > 0.5) {
                        quantityToBuy = 1;
                    }
                }

                if (quantityToBuy > item.getQuantity()) {
                    quantityToBuy = item.getQuantity();
                }

                if (quantityToBuy > 0) {
                    BigDecimal pricePerUnit = salesPrice;
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
                    UserEntity botUser = userRepository.findByUsername("vestoria")
                            .orElseThrow(() -> new ResourceNotFoundException("vestoria user not found"));

                    // Record Transaction
                    TransactionEntity transaction = TransactionEntity.builder().type(TransactionType.SYSTEM_SELL)
                            .buyer(botUser) // System (Bot) is buyer
                            .seller(owner).marketItem(null) // Direct sale
                            .price(totalEarnings).amount(quantityToBuy).itemName(item.getName()) // Store item name
                            .build();
                    transactionRepository.save(transaction);

                    // Add to summary
                    salesSummary.append(String.format("%d adet %s, ", quantityToBuy, item.getName(), finalScore));
                } else {
                    // Just save the score if we didn't buy anything
                    itemRepository.save(item);
                }
            }
        }

        // Send summary notification if any sales occurred
        if (totalBatchEarnings.compareTo(BigDecimal.ZERO) > 0) {
            String summary = salesSummary.toString();
            if (summary.endsWith(", ")) {
                summary = summary.substring(0, summary.length() - 2);
            }
            String notificationMessage = String.format("%s dükkanında satış yapıldı: %s. Toplam Kazanç: %s",
                    shop.getName(), summary, totalBatchEarnings);
            notificationService.createNotification(shop.getOwner(), notificationMessage);
        }

        // Reset shop status
        shop.setIsSelling(false);
        shop.setSalesEndsAt(null);
        shop.setLastRevenue(totalBatchEarnings);
        buildingRepository.save(shop);
    }
}
