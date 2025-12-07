package io.vestoria.service;

import io.vestoria.converter.BuildingConverter;
import io.vestoria.dto.response.BuildingConfigDto;
import io.vestoria.entity.BuildingEntity;
import io.vestoria.entity.ItemEntity;
import io.vestoria.entity.UserEntity;
import io.vestoria.enums.BuildingStatus;
import io.vestoria.enums.BuildingSubType;
import io.vestoria.enums.BuildingTier;
import io.vestoria.enums.BuildingType;
import io.vestoria.enums.ItemTier;
import io.vestoria.enums.ItemUnit;
import io.vestoria.exception.BusinessRuleException;
import io.vestoria.exception.InsufficientBalanceException;
import io.vestoria.exception.ResourceNotFoundException;
import io.vestoria.exception.UnauthorizedAccessException;
import io.vestoria.repository.BuildingRepository;
import io.vestoria.repository.ItemRepository;
import io.vestoria.repository.MarketRepository;
import io.vestoria.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;
    private final BuildingConverter buildingConverter;
    private final NotificationService notificationService;
    private final MarketRepository marketRepository;
    private final InventoryService inventoryService;
    private final ItemRepository itemRepository;

    @Transactional
    public void startSales(@NonNull UUID buildingId, @NonNull String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        BuildingEntity building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("İşletme bulunamadı"));

        if (!building.getOwner().getUsername().equals(user.getUsername())) {
            throw new UnauthorizedAccessException("Bu işletme size ait değil");
        }

        if (building.getType() != BuildingType.SHOP) {
            throw new BusinessRuleException("Sadece dükkanlarda satış başlatılabilir");
        }

        if (Boolean.TRUE.equals(building.getIsSelling())) {
            throw new BusinessRuleException("Zaten satış yapılıyor");
        }

        int currentStock = building.getItems() != null
                ? building.getItems().stream().mapToInt(ItemEntity::getQuantity).sum()
                : 0;
        if (currentStock <= 0) {
            throw new BusinessRuleException("Satılacak ürün yok. Önce ürün eklemelisiniz.");
        }

        // Start 10 minute timer
        building.setIsSelling(true);
        float duration = getProductionDuration(building.getType(), building.getTier());
        building.setSalesEndsAt(LocalDateTime.now().plusSeconds((long) (duration * 60)));
        buildingRepository.save(building);
    }

    @Transactional
    public void startProduction(@NonNull UUID buildingId, @NonNull String username, @NonNull String productId) {
        BuildingEntity building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("İşletme bulunamadı"));

        if (!building.getOwner().getUsername().equals(username)) {
            throw new UnauthorizedAccessException("Bu işletme size ait değil");
        }

        if (building.getType() == BuildingType.SHOP) {
            throw new BusinessRuleException("Dükkanlarda üretim yapılamaz");
        }

        if (Boolean.TRUE.equals(building.getIsProducing())) {
            throw new BusinessRuleException("Zaten üretim yapılıyor");
        }

        // Check costs (simplified for now, can be expanded)
        float durationMinutes = getProductionDuration(building.getType(), building.getTier());
        long seconds = (long) (durationMinutes * 60);

        // Find or create the item and mark it as producing
        ItemEntity item = building.getItems().stream().filter(i -> i.getName().equals(productId)).findFirst()
                .orElse(null);

        if (item == null) {
            // Check slot limit
            int currentSlots = building.getItems().size();
            int maxSlots = getMaxSlots(building.getTier());

            if (currentSlots >= maxSlots) {
                throw new BusinessRuleException("Depo çeşit sınırına ulaşıldı! (Bu seviye için Max: " + maxSlots
                        + " çeşit) Burdaki ürünleri genel envantere aktarıp yeni ürün üretebilirsiniz.");
            }

            ItemUnit unit = ItemUnit.PIECE;
            if (building.getType() == BuildingType.FARM) {
                unit = ItemUnit.KG;
            }

            ItemTier itemTier = ItemTier.LOW;
            BigDecimal qualityScore = BigDecimal.valueOf(50); // Default for SMALL/LOW

            if (building.getTier() == BuildingTier.MEDIUM) {
                itemTier = ItemTier.MEDIUM;
                qualityScore = BigDecimal.valueOf(75);
            }
            if (building.getTier() == BuildingTier.LARGE) {
                itemTier = ItemTier.HIGH;
                qualityScore = BigDecimal.valueOf(100);
            }

            // Luck factor for quality: -5 to +5
            double qualityLuck = (Math.random() * 20) - 10;
            qualityScore = qualityScore.add(BigDecimal.valueOf(qualityLuck));

            // Level factor for quality: +0.2 per level
            int userLevel = building.getOwner().getLevel() != null ? building.getOwner().getLevel() : 1;
            double levelBonus = userLevel * 0.2;
            qualityScore = qualityScore.add(BigDecimal.valueOf(levelBonus));

            // Clamp quality (min 10, max 100)
            if (qualityScore.compareTo(BigDecimal.TEN) < 0)
                qualityScore = BigDecimal.TEN;
            if (qualityScore.compareTo(BigDecimal.valueOf(100)) > 0)
                qualityScore = BigDecimal.valueOf(100);

            item = ItemEntity.builder().name(productId).quantity(0).building(building).owner(building.getOwner())
                    .unit(unit).tier(itemTier).qualityScore(qualityScore).build();
            building.getItems().add(item);
        }

        item.setIsProducing(true);

        building.setIsProducing(true);
        building.setProductionEndsAt(LocalDateTime.now().plusSeconds(seconds));
        buildingRepository.save(building);
    }

    @Transactional
    public void collectProduction(@NonNull UUID buildingId, @NonNull String username) {
        BuildingEntity building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("İşletme bulunamadı"));

        if (!building.getOwner().getUsername().equals(username)) {
            throw new UnauthorizedAccessException("Bu işletme size ait değil");
        }

        if (!Boolean.TRUE.equals(building.getIsProducing())) {
            throw new BusinessRuleException("Üretim yapılmıyor");
        }

        if (building.getProductionEndsAt().isAfter(java.time.LocalDateTime.now())) {
            throw new BusinessRuleException("Üretim henüz tamamlanmadı");
        }

        // Find the item that was producing
        ItemEntity item = building.getItems().stream().filter(i -> Boolean.TRUE.equals(i.getIsProducing())).findFirst()
                .orElse(null);

        if (item == null) {
            building.setIsProducing(false);
            building.setProductionEndsAt(null);
            buildingRepository.save(building);
            throw new BusinessRuleException("Üretilen ürün bulunamadı.");
        }

        int baseQuantity = building.getProductionRate().intValue();

        // Luck factor for quantity: 0.90 to 1.15 (-10% to +15%)
        double luck = 0.90 + (Math.random() * 0.25);

        // Level factor for quantity: +1% per level
        int userLevel = building.getOwner().getLevel() != null ? building.getOwner().getLevel() : 1;
        double levelMultiplier = 1.0 + (userLevel * 0.01);

        int quantity = (int) (baseQuantity * luck * levelMultiplier);

        if (quantity < 1)
            quantity = 1;

        item.setQuantity(item.getQuantity() + quantity);
        item.setIsProducing(false);

        // Reset status
        building.setIsProducing(false);
        building.setProductionEndsAt(null);
        buildingRepository.save(building);

        // Notify user
        String message = String.format("%s işletmesinden %d %s %s toplandı.", building.getName(), quantity,
                item.getUnit() == ItemUnit.KG ? "kg" : "adet", item.getName());
        notificationService.createNotification(building.getOwner(), message);
    }

    @Transactional
    public void withdrawFromBuilding(@NonNull UUID buildingId, @NonNull String username, @NonNull String productId,
                                     int quantity) {
        BuildingEntity building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("İşletme bulunamadı"));

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        if (!building.getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Bu işletme size ait değil");
        }

        if (quantity <= 0) {
            throw new BusinessRuleException("Geçersiz miktar");
        }

        ItemEntity item = building.getItems().stream().filter(i -> i.getName().equals(productId)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Ürün bulunamadı"));

        if (item.getQuantity() < quantity) {
            throw new BusinessRuleException("En fazla işletmenizde var olan miktar kadar transfer yapabilirsiniz.");
        }

        // Decrease building stock
        item.setQuantity(item.getQuantity() - quantity);

        // Add to user inventory
        inventoryService.addItemToInventory(user, productId, quantity, item.getUnit(), item.getTier(),
                item.getQualityScore());

        if (item.getQuantity() == 0) {
            building.getItems().remove(item);
            itemRepository.delete(item);
        }

        buildingRepository.save(building);
    }

    @Transactional
    @CacheEvict(value = "getUserBuildings", allEntries = true)
    public BuildingEntity createBuilding(UserEntity owner, String name, BuildingType type, BuildingTier tier,
                                         BuildingSubType subType) {

        // Validation: SHOP requires SubType
        if (type == BuildingType.SHOP && subType == null) {
            throw new BusinessRuleException("Dükkan açarken türünü (Market, Kuyumcu vb.) seçmelisiniz.");
        }

        // Validation: Check for duplicate name for same type and owner
        if (buildingRepository.existsByNameAndTypeAndOwnerId(name, type, owner.getId())) {
            throw new BusinessRuleException("Bu isimde ve tipte bir işletmeniz zaten var.");
        }

        // Calculate cost based on tier
        BigDecimal cost = getBuildingCost(type, tier);

        if (owner.getBalance().compareTo(cost) < 0) {
            throw new InsufficientBalanceException("Yetersiz bakiye! Bu işlem için " + cost + " TL gerekiyor.");
        }

        owner.setBalance(owner.getBalance().subtract(cost));
        userRepository.save(owner);

        BuildingEntity building = BuildingEntity.builder().owner(owner).name(name).type(type).tier(tier)
                .subType(subType) // Can be null for non-SHOP
                .productionRate(getProductionRate(type, tier)).maxSlots(getMaxSlots(tier)).status(BuildingStatus.ACTIVE)
                .cost(cost).maxStock(getStorageCapacity(type, tier)).build();

        return buildingRepository.save(building);
    }

    @Transactional
    public BuildingEntity createBuilding(String username, String name, BuildingType type, BuildingTier tier,
                                         BuildingSubType subType) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + username));
        return createBuilding(user, name, type, tier, subType);
    }

    @Transactional
    public BuildingEntity upgradeBuilding(UUID buildingId, String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + username));
        return upgradeBuilding(buildingId, user.getId());
    }

    @Transactional
    @CacheEvict(value = "getUserBuildings", allEntries = true)
    public BuildingEntity upgradeBuilding(UUID buildingId, UUID userId) {
        BuildingEntity building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("Bina bulunamadı"));

        if (!building.getOwner().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Bu binayı yükseltme yetkiniz yok.");
        }

        if (building.getTier().value >= 3) {
            throw new BusinessRuleException("Bina zaten maksimum seviyede (Seviye 3).");
        }

        BigDecimal upgradeCost = new BigDecimal("15000");
        UserEntity user = building.getOwner();

        if (user.getBalance().compareTo(upgradeCost) < 0) {
            throw new InsufficientBalanceException("Yetersiz bakiye! Yükseltme için 15,000 TL gerekiyor.");
        }

        user.setBalance(user.getBalance().subtract(upgradeCost));
        userRepository.save(user);

        int nextLevel = building.getTier().value + 1;
        building.setTier(BuildingTier.fromValue(nextLevel));
        building.setProductionRate(getProductionRate(building.getType(), building.getTier()));
        building.setMaxStock(getStorageCapacity(building.getType(), building.getTier()));

        return buildingRepository.save(building);
    }

    @Transactional
    public void closeBuilding(UUID buildingId, String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + username));
        closeBuilding(buildingId, user.getId());
    }

    @Transactional
    @CacheEvict(value = "getUserBuildings", allEntries = true)
    public void closeBuilding(UUID buildingId, UUID userId) {
        BuildingEntity building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("Bina bulunamadı"));

        if (!building.getOwner().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Bu binayı kapatma yetkiniz yok.");
        }

        // Check if building has any inventory items
        if (building.getItems() != null && !building.getItems().isEmpty()) {
            int totalItems = building.getItems().stream().mapToInt(ItemEntity::getQuantity).sum();
            if (totalItems > 0) {
                throw new BusinessRuleException(
                        "Binada ürün bulunuyor. Önce tüm ürünleri satmalısınız veya kullanmalısınız.");
            }
        }

        // Check if building has any active market listings
        long activeListings = marketRepository.countActiveListingsByBuilding(buildingId);
        if (activeListings > 0) {
            throw new BusinessRuleException(
                    "Bu binaya ait pazarda aktif ilanlar bulunuyor. Önce ilanları iptal etmelisiniz.");
        }

        // Calculate refund (building cost - 10,000 TL)
        BigDecimal buildingCost = getBuildingCost(building.getType(), building.getTier());
        BigDecimal refund = buildingCost.subtract(new BigDecimal("10000"));

        // Ensure refund is not negative
        if (refund.compareTo(BigDecimal.ZERO) < 0) {
            refund = BigDecimal.ZERO;
        }

        // Refund to user
        UserEntity user = building.getOwner();
        user.setBalance(user.getBalance().add(refund));
        userRepository.save(user);

        // Delete building
        buildingRepository.delete(building);
    }

    @Transactional
    public BuildingEntity setProduction(@NonNull UUID buildingId, @NonNull String username,
                                        @NonNull BuildingSubType productionType) {

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        BuildingEntity building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("Bina bulunamadı"));

        if (!building.getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Bu bina size ait değil.");
        }

        if (building.getType() == BuildingType.SHOP) {
            throw new BusinessRuleException("Dükkanların üretim türü değiştirilemez.");
        }

        if (productionType.getParentType() != building.getType()) {
            throw new BusinessRuleException("Seçilen üretim türü bu bina tipi için uygun değil.");
        }

        building.setSubType(productionType);
        return buildingRepository.save(building);
    }

    public List<BuildingEntity> getUserBuildings(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        return getUserBuildings(user.getId());
    }

    @Cacheable(value = "getUserBuildings", key = "#userId")
    public List<BuildingEntity> getUserBuildings(UUID userId) {
        return buildingRepository.findByOwnerId(userId);
    }

    @Cacheable("buildingConfigs")
    public List<BuildingConfigDto> getBuildingConfigs() {
        List<BuildingConfigDto> configs = new ArrayList<>();
        for (BuildingType type : BuildingType.values()) {
            for (BuildingTier tier : BuildingTier.values()) {
                configs.add(buildingConverter.toConfigDto(type, tier, getBuildingCost(type, tier),
                        getProductionRate(type, tier), getProductionDuration(type, tier), getSalesDuration(tier),
                        getStorageCapacity(type, tier), getMaxSlots(tier)));
            }
        }
        return configs;
    }

    private BigDecimal getBuildingCost(BuildingType type, BuildingTier tier) {
        // Base costs for SMALL tier (in thousands)
        BigDecimal baseCost = switch (type) {
            case SHOP -> BigDecimal.valueOf(25000);
            case GARDEN -> BigDecimal.valueOf(35000);
            case FARM -> BigDecimal.valueOf(45000);
            case FACTORY -> BigDecimal.valueOf(55000);
            case MINE -> BigDecimal.valueOf(65000);
        };

        if (tier != null) {
            switch (tier) {
                case MEDIUM:
                    baseCost = baseCost.add(BigDecimal.valueOf(10000));
                    break;
                case LARGE:
                    baseCost = baseCost.add(BigDecimal.valueOf(25000));
                    break;
                default: // SMALL
                    break;
            }
        }

        return baseCost;
    }

    private BigDecimal getProductionRate(BuildingType type, BuildingTier tier) {
        // Base production rate increases from Garden to Mine
        BigDecimal rate = switch (type) {
            case SHOP -> BigDecimal.ZERO; // Shops don't produce
            case GARDEN -> BigDecimal.valueOf(20);
            case FARM -> BigDecimal.valueOf(25);
            case FACTORY -> BigDecimal.valueOf(40);
            case MINE -> BigDecimal.valueOf(30);
        };

        // Tier Multiplier
        if (tier != null && type != BuildingType.SHOP) {
            switch (tier) {
                case MEDIUM:
                    rate = rate.multiply(BigDecimal.valueOf(1.5));
                    break;
                case LARGE:
                    rate = rate.multiply(BigDecimal.valueOf(2.0));
                    break;
                default:
                    break;
            }
        }

        return rate;
    }

    private Integer getStorageCapacity(BuildingType type, BuildingTier tier) {
        // Base storage
        int base;

        if (type == BuildingType.SHOP) {
            // Shops need high storage for selling
            base = 500;
        } else {
            // Production buildings storage
            base = switch (type) {
                case FARM -> 500;
                case FACTORY -> 600;
                case MINE -> 800;
                default -> 250;
            };
        }

        if (tier != null) {
            switch (tier) {
                case MEDIUM:
                    base = (int) (base * 2.0);
                    break;
                case LARGE:
                    base = (int) (base * 5.0);
                    break;
                default:
                    break;
            }
        }
        return base;
    }

    private float getProductionDuration(BuildingType type, BuildingTier tier) {
        long baseMinutes = switch (type) {
            case GARDEN -> 12;
            case FARM -> 15;
            case FACTORY -> 20;
            case MINE -> 25;
            default -> 10;
        };

        if (tier != null) {
            switch (tier) {
                case MEDIUM:
                    baseMinutes = (long) (baseMinutes * 0.8);
                    break;
                case LARGE:
                    baseMinutes = (long) (baseMinutes * 0.6);
                    break;
                default:
                    break;
            }
        }

        return Math.max(1, baseMinutes); // Minimum 1 minute
    }

    private float getSalesDuration(BuildingTier tier) {
        float baseMinutes = 10;
        if (tier != null) {
            switch (tier) {
                case MEDIUM:
                    baseMinutes = (long) (baseMinutes * 0.8);
                    break;
                case LARGE:
                    baseMinutes = (long) (baseMinutes * 0.6);
                    break;
                default:
                    break;
            }
        }
        return baseMinutes;
    }

    private Integer getMaxSlots(BuildingTier tier) {
        if (tier == null)
            return 1;
        return switch (tier) {
            case MEDIUM -> 2;
            case LARGE -> 4;
            default -> 1;
        };
    }
}
