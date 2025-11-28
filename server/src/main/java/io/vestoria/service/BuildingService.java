package io.vestoria.service;

import io.vestoria.entity.BuildingEntity;
import io.vestoria.entity.ItemEntity;
import io.vestoria.entity.UserEntity;
import io.vestoria.enums.BuildingType;
import io.vestoria.enums.BuildingTier;
import io.vestoria.enums.BuildingStatus;
import io.vestoria.enums.BuildingSubType;
import io.vestoria.repository.BuildingRepository;
import io.vestoria.repository.UserRepository;
import io.vestoria.repository.MarketRepository;
import io.vestoria.exception.InsufficientBalanceException;
import io.vestoria.exception.ResourceNotFoundException;
import io.vestoria.exception.UnauthorizedAccessException;
import io.vestoria.exception.BusinessRuleException;
import io.vestoria.converter.BuildingConverter;
import io.vestoria.dto.response.BuildingConfigDto;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;
    private final BuildingConverter buildingConverter;
    private final MarketRepository marketRepository;

    @Transactional
    public void startSales(UUID buildingId, String username) {
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
    public void startProduction(UUID buildingId, String username, String productId) {
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
        ItemEntity item = building.getItems().stream()
                .filter(i -> i.getName().equals(productId))
                .findFirst()
                .orElse(null);

        if (item == null) {
            // Algorithm to calculate initial values
            io.vestoria.enums.ItemUnit unit = io.vestoria.enums.ItemUnit.PIECE;
            if (building.getType() == BuildingType.FARM) {
                unit = io.vestoria.enums.ItemUnit.KG;
            }

            io.vestoria.enums.ItemTier itemTier = io.vestoria.enums.ItemTier.LOW;
            BigDecimal qualityScore = BigDecimal.valueOf(50); // Default for SMALL/LOW

            if (building.getTier() == BuildingTier.MEDIUM) {
                itemTier = io.vestoria.enums.ItemTier.MEDIUM;
                qualityScore = BigDecimal.valueOf(75);
            }
            if (building.getTier() == BuildingTier.LARGE) {
                itemTier = io.vestoria.enums.ItemTier.HIGH;
                qualityScore = BigDecimal.valueOf(100);
            }

            item = ItemEntity.builder()
                    .name(productId)
                    .quantity(0)
                    .building(building)
                    .owner(building.getOwner())
                    .unit(unit)
                    .tier(itemTier)
                    .qualityScore(qualityScore)
                    .build();
            building.getItems().add(item);
        }

        item.setIsProducing(true);

        building.setIsProducing(true);
        building.setProductionEndsAt(LocalDateTime.now().plusSeconds(seconds));
        buildingRepository.save(building);
    }

    @Transactional
    public void collectProduction(UUID buildingId, String username) {
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
        ItemEntity item = building.getItems().stream()
                .filter(i -> Boolean.TRUE.equals(i.getIsProducing()))
                .findFirst()
                .orElse(null);

        if (item == null) {
            // Fallback if something went wrong, maybe log it
            // For now, we can't produce anything if we don't know what it is.
            // But to avoid stuck state, we reset building.
            building.setIsProducing(false);
            building.setProductionEndsAt(null);
            buildingRepository.save(building);
            throw new BusinessRuleException("Üretilen ürün bulunamadı.");
        }

        int quantity = building.getProductionRate().intValue();
        if (quantity < 1)
            quantity = 1;

        item.setQuantity(item.getQuantity() + quantity);
        item.setIsProducing(false);

        // Reset status
        building.setIsProducing(false);
        building.setProductionEndsAt(null);
        buildingRepository.save(building);
    }

    @Transactional
    @SuppressWarnings("null")
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
            throw new InsufficientBalanceException(
                    "Yetersiz bakiye! Bu işlem için " + cost + " TL gerekiyor.");
        }

        owner.setBalance(owner.getBalance().subtract(cost));
        userRepository.save(owner);

        BuildingEntity building = BuildingEntity.builder()
                .owner(owner)
                .name(name)
                .type(type)
                .tier(tier)
                .subType(subType) // Can be null for non-SHOP
                .productionRate(getProductionRate(type, tier))
                .maxSlots(getMaxSlots(tier))
                .status(BuildingStatus.ACTIVE)
                .cost(cost)
                .maxStock(getStorageCapacity(type, tier))
                .build();

        return buildingRepository.save(building);
    }

    @Transactional
    public BuildingEntity createBuilding(String username, String name, BuildingType type, BuildingTier tier,
            BuildingSubType subType) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + username));
        return createBuilding(user, name, type, tier, subType);
    }

    @Transactional
    public BuildingEntity upgradeBuilding(UUID buildingId, String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + username));
        return upgradeBuilding(buildingId, user.getId());
    }

    @Transactional
    @SuppressWarnings("null")
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
            throw new InsufficientBalanceException(
                    "Yetersiz bakiye! Yükseltme için 15,000 TL gerekiyor.");
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
    @SuppressWarnings("null")
    public void closeBuilding(UUID buildingId, UUID userId) {
        BuildingEntity building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("Bina bulunamadı"));

        if (!building.getOwner().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Bu binayı kapatma yetkiniz yok.");
        }

        // Check if building has any inventory items
        if (building.getItems() != null && !building.getItems().isEmpty()) {
            int totalItems = building.getItems().stream()
                    .mapToInt(ItemEntity::getQuantity)
                    .sum();
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

    public List<BuildingEntity> getUserBuildings(UUID userId) {
        return buildingRepository.findByOwnerId(userId);
    }

    @Cacheable("buildingConfigs")
    public List<BuildingConfigDto> getBuildingConfigs() {
        List<BuildingConfigDto> configs = new ArrayList<>();
        for (BuildingType type : BuildingType.values()) {
            for (BuildingTier tier : BuildingTier.values()) {
                configs.add(buildingConverter.toConfigDto(
                        type,
                        tier,
                        getBuildingCost(type, tier),
                        getProductionRate(type, tier),
                        getProductionDuration(type, tier),
                        getSalesDuration(tier),
                        getStorageCapacity(type, tier),
                        getMaxSlots(tier)));
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
            case GARDEN -> BigDecimal.valueOf(10);
            case FARM -> BigDecimal.valueOf(20);
            case FACTORY -> BigDecimal.valueOf(40);
            case MINE -> BigDecimal.valueOf(50);
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
                case FARM -> 350;
                case FACTORY -> 500;
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
            case GARDEN -> 15;
            case FARM -> 20;
            case FACTORY -> 25;
            case MINE -> 30;
            default -> 10;
        };

        if (tier != null) {
            switch (tier) {
                case MEDIUM:
                    baseMinutes = (long) (baseMinutes * 0.9); // 10% faster
                    break;
                case LARGE:
                    baseMinutes = (long) (baseMinutes * 0.8); // 20% faster
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
                    baseMinutes = (long) (baseMinutes * 0.9); // 10% faster
                    break;
                case LARGE:
                    baseMinutes = (long) (baseMinutes * 0.8); // 20% faster
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
