package io.vestoria.service;

import io.vestoria.converter.BuildingConverter;
import io.vestoria.dto.request.CreateBuildingRequestDto;
import io.vestoria.entity.BuildingEntity;
import io.vestoria.entity.UserEntity;
import io.vestoria.enums.BuildingType;
import io.vestoria.exception.LimitExceededException;
import io.vestoria.exception.NotFoundException;
import io.vestoria.repository.BuildingRepository;
import io.vestoria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BuildingService {
    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;
    private final BuildingConverter buildingConverter;

    public ResponseEntity<?> createBuilding(CreateBuildingRequestDto request) {
        UserEntity userEntity = userRepository.findById(UUID.fromString(request.ownerId()))
                .orElseThrow(() -> new NotFoundException("Kullanıcıya ait bilgilere ulaşılamadı!"));

        BigDecimal cost = calculateCost(request.level(), request.type());

        if (userEntity.getBalance().compareTo(cost) < 0) {
            throw new LimitExceededException("Bakiye yetersiz'");
        }

        BuildingEntity buildingEntity = BuildingEntity.builder()
                .type(request.type())
                .subType(request.subType())
                .owner(userEntity)
                .items(Collections.emptyList())
                .cost(calculateCost(request.level(), request.type()))
                .maxStock(calculateMaxStock(request.level(), request.type()))
                .productionRate(calculateProductionRate(request.level(), request.type()))
                .level(request.level())
                .status("ACTIVE")
                .build();

        BuildingEntity savedBuilding = buildingRepository.save(buildingEntity);

        // building kaydedildikten sonra kullanıcının balance düşürülür
        BigDecimal subtract = userEntity.getBalance().subtract(cost);
        userEntity.setBalance(subtract);
        userRepository.save(userEntity);

        return ResponseEntity.ok(buildingConverter.toDto(savedBuilding));
    }

    private BigDecimal calculateCost(int level, BuildingType type) {
        if (type == null || level < 1 || level > 3) {
            return BigDecimal.ZERO;
        }

        long baseCost = 0;

        switch (type) {
            case SHOP:
                baseCost = 25_000;
                break;
            case GARDEN:
                baseCost = 35_000;
                break;
            case FARM:
                baseCost = 45_000;
                break;
            case FACTORY:
                baseCost = 60_000;
                break;
            case MINE:
                baseCost = 65_000;
                break;
            default:
                return BigDecimal.ZERO;
        }

        long levelIncrement = 0;
        switch (level) {
            case 1:
                break;
            case 2:
                levelIncrement = 10_000;
                break;
            case 3:
                levelIncrement = 25_000;
                break;
        }

        return BigDecimal.valueOf(baseCost + levelIncrement);
    }

    private Integer calculateMaxStock(int level, BuildingType type) {
        if (type == null || level < 1 || level > 3) {
            return 0;
        }

        int baseStock = 0;

        switch (type) {
            case SHOP:
                baseStock = 500;
                break;
            case GARDEN:
                baseStock = 1_000;
                break;
            case FARM:
                baseStock = 1_500;
                break;
            case FACTORY:
                baseStock = 2_000;
                break;
            case MINE:
                baseStock = 2_500;
                break;
            default:
                return 0;
        }

        int levelIncrement = 0;
        switch (level) {
            case 1:
                break;
            case 2:
                levelIncrement = 500;
                break;
            case 3:
                levelIncrement = 1_000;
                break;
        }

        return baseStock + levelIncrement;
    }

    private Long calculateProductionRate(int level, BuildingType type) {
        if (type == null || level < 1 || level > 3) {
            return 0L;
        }

        long baseTime = 900_000L; // 15 dakika

        switch (type) {
            case SHOP:
                break;
            case GARDEN:
                baseTime = 720_000L; // 12 dakika
                break;
            case FARM:
                baseTime = 900_000L; // 15 dakika
                break;
            case FACTORY:
                baseTime = 1_080_000L; // 18 dakika
                break;
            case MINE:
                baseTime = 1_200_000L; // 20 dakika
                break;
            default:
                return 0L;
        }

        long levelDecrement = 0L;
        switch (level) {
            case 1:
                break;
            case 2:
                levelDecrement = 120_000L; // 2 dakika
                break;
            case 3:
                levelDecrement = 300_000L; // 5 dakika
                break;
        }

        return baseTime - levelDecrement;
    }
}
