package io.vestoria.service;

import io.vestoria.entity.BuildingEntity;
import io.vestoria.entity.UserEntity;
import io.vestoria.enums.BuildingType;
import io.vestoria.enums.BuildingTier;
import io.vestoria.enums.BuildingStatus;
import io.vestoria.enums.BuildingSubType;
import io.vestoria.repository.BuildingRepository;
import io.vestoria.repository.UserRepository;
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

        if (building.getLevel() >= 3) {
            throw new BusinessRuleException("Bina zaten maksimum seviyede (Seviye 3).");
        }

        int nextLevel = building.getLevel() + 1;
        BigDecimal upgradeCost = getBuildingCost(building.getType(), building.getTier(), nextLevel);
        UserEntity user = building.getOwner();

        if (user.getBalance().compareTo(upgradeCost) < 0) {
            throw new InsufficientBalanceException(
                    "Yetersiz bakiye! Yükseltme için " + upgradeCost + " TL gerekiyor.");
        }

        user.setBalance(user.getBalance().subtract(upgradeCost));
        userRepository.save(user);

        building.setLevel(nextLevel);
        building.setProductionRate(getProductionRate(building.getType(), building.getTier(), nextLevel));
        building.setMaxStock(getStorageCapacity(building.getType(), building.getTier(), nextLevel));
        building.setStatus(BuildingStatus.UPGRADING); // Set status to UPGRADING during upgrade if needed, or keep
                                                      // ACTIVE

        return buildingRepository.save(building);
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
