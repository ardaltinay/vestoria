package io.vestoria.service;

import io.vestoria.constant.Constants;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // FACTORY: Check if required raw materials are available
        if (building.getType() == BuildingType.FACTORY) {
            List<String> requiredMaterials = Constants.FACTORY_MAP.get(productId);
            if (requiredMaterials != null && !requiredMaterials.isEmpty()) {
                // Tier-based material cost
                int materialCost = switch (building.getTier()) {
                    case SMALL -> 3;
                    case MEDIUM -> 5;
                    case LARGE -> 8;
                };

                // Get user's central inventory items
                List<ItemEntity> centralInventory = itemRepository
                        .findByOwnerIdAndBuildingIsNull(building.getOwner().getId());

                for (String material : requiredMaterials) {
                    // Check in factory inventory
                    int factoryQty = building.getItems().stream()
                            .filter(i -> i.getName().equalsIgnoreCase(material))
                            .mapToInt(ItemEntity::getQuantity)
                            .sum();

                    // Check in central inventory
                    int centralQty = centralInventory.stream()
                            .filter(i -> i.getName().equalsIgnoreCase(material))
                            .mapToInt(ItemEntity::getQuantity)
                            .sum();

                    int totalQty = factoryQty + centralQty;

                    if (totalQty < materialCost) {
                        throw new BusinessRuleException("Hammadde eksik: " + material
                                + ". Bu ürünü üretmek için envanterinizde en az " + materialCost + " adet " + material
                                + " bulunmalıdır. (Mevcut: " + totalQty + ")");
                    }
                }
            }
        }

        // Check costs (simplified for now, can be expanded)
        float durationMinutes = getProductionDuration(building.getType(), building.getTier());
        long seconds = (long) (durationMinutes * 60);

        // 1. Calculate Quality Score FIRST to check for existing matches
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

        // Luck factor for quality: -10 to +10
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

        // Round to 2 decimal places to make exact matching possible/realistic
        qualityScore = qualityScore.setScale(2, java.math.RoundingMode.HALF_UP);

        // 2. Check if an item with the EXACT same name and quality already exists
        final BigDecimal finalQuality = qualityScore;
        ItemEntity item = building.getItems().stream()
                .filter(i -> i.getName().trim().equalsIgnoreCase(productId.trim())
                        && i.getQualityScore().compareTo(finalQuality) == 0)
                .findFirst()
                .orElse(null);

        if (item == null) {
            // New batch required

            // Validation: Check if we have room for a new item type (if it's a new type)
            long distinctProductTypeCount = building.getItems().stream()
                    .map(i -> i.getName().trim().toLowerCase())
                    .distinct()
                    .count();

            boolean isNewType = building.getItems().stream()
                    .noneMatch(i -> i.getName().trim().equalsIgnoreCase(productId.trim()));

            int maxSlots = getMaxSlots(building.getTier());

            if (isNewType && distinctProductTypeCount >= maxSlots) {
                throw new BusinessRuleException("Depo çeşit sınırına ulaşıldı! (Bu seviye için Max: " + maxSlots
                        + " çeşit) Farklı bir ürün üretmek için mevcut çeşitlerden birini tamamen boşaltmalısınız.");
            }

            ItemUnit unit = ItemUnit.PIECE;
            if (building.getType() == BuildingType.FARM) {
                unit = ItemUnit.KG;
            }

            item = ItemEntity.builder()
                    .name(productId)
                    .quantity(0) // Starts at 0, filled at collection
                    .building(building)
                    .owner(building.getOwner())
                    .unit(unit)
                    .tier(itemTier)
                    .qualityScore(finalQuality)
                    .isProducing(true)
                    .build();

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

        // FACTORY: Consume raw materials from factory inventory or central inventory
        if (building.getType() == BuildingType.FACTORY) {
            List<String> requiredMaterials = Constants.FACTORY_MAP.get(item.getName());
            if (requiredMaterials != null && !requiredMaterials.isEmpty()) {
                // Tier-based material cost
                int materialCost = switch (building.getTier()) {
                    case SMALL -> 3;
                    case MEDIUM -> 5;
                    case LARGE -> 8;
                };

                List<ItemEntity> centralInventory = itemRepository
                        .findByOwnerIdAndBuildingIsNull(building.getOwner().getId());

                for (String material : requiredMaterials) {
                    int remaining = materialCost;

                    // Try factory inventory first
                    ItemEntity factoryMaterial = building.getItems().stream()
                            .filter(i -> i.getName().equalsIgnoreCase(material) && i.getQuantity() > 0)
                            .findFirst()
                            .orElse(null);

                    if (factoryMaterial != null && factoryMaterial.getQuantity() > 0) {
                        int consumeFromFactory = Math.min(factoryMaterial.getQuantity(), remaining);
                        factoryMaterial.setQuantity(factoryMaterial.getQuantity() - consumeFromFactory);
                        remaining -= consumeFromFactory;

                        if (factoryMaterial.getQuantity() == 0) {
                            building.getItems().remove(factoryMaterial);
                            itemRepository.delete(factoryMaterial);
                        }
                    }

                    // If still need more, take from central inventory
                    if (remaining > 0) {
                        ItemEntity centralMaterial = centralInventory.stream()
                                .filter(i -> i.getName().equalsIgnoreCase(material) && i.getQuantity() > 0)
                                .findFirst()
                                .orElse(null);

                        if (centralMaterial != null && centralMaterial.getQuantity() > 0) {
                            int consumeFromCentral = Math.min(centralMaterial.getQuantity(), remaining);
                            centralMaterial.setQuantity(centralMaterial.getQuantity() - consumeFromCentral);

                            if (centralMaterial.getQuantity() == 0) {
                                itemRepository.delete(centralMaterial);
                            } else {
                                itemRepository.save(centralMaterial);
                            }
                        }
                    }
                }
            }
        }

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
    public void withdrawFromBuilding(@NonNull UUID buildingId, @NonNull String username, @NonNull String itemIdStr,
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

        UUID itemId = UUID.fromString(itemIdStr);
        ItemEntity item = building.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Ürün bulunamadı"));

        if (item.getQuantity() < quantity) {
            throw new BusinessRuleException("En fazla işletmenizde var olan miktar kadar transfer yapabilirsiniz.");
        }

        // Decrease building stock
        item.setQuantity(item.getQuantity() - quantity);

        // Add to user inventory
        inventoryService.addItemToInventory(user, item.getName(), quantity, item.getUnit(), item.getTier(),
                item.getQualityScore());

        if (item.getQuantity() == 0) {
            building.getItems().remove(item);
            itemRepository.delete(item);
        }

        buildingRepository.save(building);
    }

    @Transactional
    public void updateItemPrice(@NonNull UUID buildingId, @NonNull UUID itemId, @NonNull String username,
            @NonNull BigDecimal price) {
        BuildingEntity building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("İşletme bulunamadı"));

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        if (!building.getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Bu işletme size ait değil");
        }

        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Fiyat 0'dan küçük olamaz.");
        }

        ItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Ürün bulunamadı"));

        if (!item.getBuilding().getId().equals(buildingId)) {
            throw new BusinessRuleException("Bu ürün bu işletmeye ait değil.");
        }

        item.setPrice(price);
        itemRepository.save(item);
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
