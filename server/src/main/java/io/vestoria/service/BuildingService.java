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
    @lombok.Getter
    private final UserRepository userRepository;
    private final BuildingConverter buildingConverter;
    private final MarketRepository marketRepository;

    @Transactional
    public void startSales(UUID buildingId, String username) {
        BuildingEntity building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("İşletme bulunamadı"));

        if (!building.getOwner().getUsername().equals(username)) {
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
        building.setSalesEndsAt(LocalDateTime.now().plusMinutes(10));
        buildingRepository.save(building);
    }

    @Transactional
    public void startProduction(UUID buildingId, String username) {
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
        long durationMinutes = getProductionDuration(building.getType(), building.getTier(), building.getLevel());

        building.setIsProducing(true);
        building.setProductionEndsAt(LocalDateTime.now().plusMinutes(durationMinutes));
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

        // Add items to inventory (Simplified logic)
        // In a real scenario, we would determine WHAT was produced.
        // For now, let's assume we produce based on building subtype.

        // Reset status
        building.setIsProducing(false);
        building.setProductionEndsAt(null);
        buildingRepository.save(building);
    }

    @Transactional
    @SuppressWarnings("null")
    public BuildingEntity createBuilding(UserEntity owner, BuildingType type, BuildingTier tier,
            BuildingSubType subType) {

        // Validation: SHOP requires SubType
        if (type == BuildingType.SHOP && subType == null) {
            throw new BusinessRuleException("Dükkan açarken türünü (Market, Kuyumcu vb.) seçmelisiniz.");
        }

        // Calculate cost based on tier
        BigDecimal cost = getBuildingCost(type, tier, 1);

        if (owner.getBalance().compareTo(cost) < 0) {
            throw new InsufficientBalanceException(
                    "Yetersiz bakiye! Bu işlem için " + cost + " TL gerekiyor.");
        }

        owner.setBalance(owner.getBalance().subtract(cost));
        userRepository.save(owner);

        BuildingEntity building = BuildingEntity.builder()
                .owner(owner)
                .type(type)
                .tier(tier)
                .subType(subType) // Can be null for non-SHOP
                .level(1)
                .productionRate(getProductionRate(type, tier, 1))
                .maxSlots(getMaxSlots(tier))
                .status(BuildingStatus.ACTIVE)
                .cost(cost)
                .maxStock(getStorageCapacity(type, tier, 1))
                .build();

        return buildingRepository.save(building);
    }

    @Transactional
    public BuildingEntity createBuilding(String username, BuildingType type, BuildingTier tier,
            BuildingSubType subType) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + username));
        return createBuilding(user, type, tier, subType);
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

        // Check max level (10)
        if (building.getLevel() >= 3) {
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

        int nextLevel = building.getLevel() + 1;
        building.setLevel(nextLevel);
        building.setProductionRate(getProductionRate(building.getType(), building.getTier(), nextLevel));
        building.setMaxStock(getStorageCapacity(building.getType(), building.getTier(), nextLevel));

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
        BigDecimal buildingCost = getBuildingCost(building.getType(), building.getTier(), building.getLevel());
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
    public BuildingEntity setProduction(@NonNull UUID buildingId, @NonNull UUID userId,
            @NonNull BuildingSubType productionType) {
        BuildingEntity building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("Bina bulunamadı"));

        if (!building.getOwner().getId().equals(userId)) {
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
                        getBuildingCost(type, tier, 1),
                        getProductionRate(type, tier, 1),
                        getStorageCapacity(type, tier, 1),
                        getMaxSlots(tier)));
            }
        }
        return configs;
    }

    private BigDecimal getBuildingCost(BuildingType type, BuildingTier tier, int level) {
        // Base costs for SMALL tier (in thousands)
        BigDecimal baseCost;
        switch (type) {
            case SHOP:
                baseCost = BigDecimal.valueOf(25000);
                break;
            case GARDEN:
                baseCost = BigDecimal.valueOf(35000);
                break;
            case FARM:
                baseCost = BigDecimal.valueOf(45000);
                break;
            case FACTORY:
                baseCost = BigDecimal.valueOf(55000);
                break;
            case MINE:
                baseCost = BigDecimal.valueOf(65000);
                break;
            default:
                baseCost = BigDecimal.valueOf(25000);
        }

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

        // Level Multiplier (Keep existing logic)
        if (level == 2)
            return baseCost.multiply(BigDecimal.valueOf(1.5));
        if (level == 3)
            return baseCost.multiply(BigDecimal.valueOf(2.5));
        return baseCost;
    }

    private BigDecimal getProductionRate(BuildingType type, BuildingTier tier, int level) {
        // Base production rate increases from Garden to Mine
        BigDecimal rate;
        switch (type) {
            case SHOP:
                rate = BigDecimal.ZERO; // Shops don't produce
                break;
            case GARDEN:
                rate = BigDecimal.valueOf(10);
                break;
            case FARM:
                rate = BigDecimal.valueOf(20);
                break;
            case FACTORY:
                rate = BigDecimal.valueOf(40);
                break;
            case MINE:
                rate = BigDecimal.valueOf(80);
                break;
            default:
                rate = BigDecimal.ONE;
        }

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

        // Level Multiplier
        if (level == 2)
            rate = rate.multiply(BigDecimal.valueOf(2));
        if (level == 3)
            rate = rate.multiply(BigDecimal.valueOf(4));

        return rate;
    }

    private Integer getStorageCapacity(BuildingType type, BuildingTier tier, int level) {
        // Base storage
        int base;

        if (type == BuildingType.SHOP) {
            // Shops need high storage for selling
            base = 500;
        } else {
            // Production buildings storage
            switch (type) {
                case GARDEN:
                    base = 100;
                    break;
                case FARM:
                    base = 200;
                    break;
                case FACTORY:
                    base = 400;
                    break;
                case MINE:
                    base = 800;
                    break;
                default:
                    base = 100;
            }
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
        return base * level;
    }

    private long getProductionDuration(BuildingType type, BuildingTier tier, int level) {
        long baseMinutes;
        switch (type) {
            case GARDEN:
                baseMinutes = 15;
                break;
            case FARM:
                baseMinutes = 20;
                break;
            case FACTORY:
                baseMinutes = 25;
                break;
            case MINE:
                baseMinutes = 30;
                break;
            default:
                baseMinutes = 10;
        }

        // Higher tier takes slightly longer but produces more (handled in production
        // rate)
        // Or maybe higher tier is faster? Let's say higher tier is same duration but
        // more output.
        // But user asked for duration to change.
        // Let's say Higher Tier = Faster? Or Slower?
        // Usually Higher Tier = More Capacity/Speed.
        // Let's make Higher Tier FASTER.
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

        // Higher Level = Faster
        if (level == 2)
            baseMinutes = (long) (baseMinutes * 0.9);
        if (level == 3)
            baseMinutes = (long) (baseMinutes * 0.8);

        return Math.max(1, baseMinutes); // Minimum 1 minute
    }

    private Integer getMaxSlots(BuildingTier tier) {
        if (tier == null)
            return 1;
        switch (tier) {
            case SMALL:
                return 1;
            case MEDIUM:
                return 2;
            case LARGE:
                return 4;
            default:
                return 1;
        }
    }
}
