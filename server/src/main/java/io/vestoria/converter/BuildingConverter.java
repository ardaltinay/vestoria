package io.vestoria.converter;

import io.vestoria.dto.response.BuildingConfigDto;
import io.vestoria.dto.response.BuildingProductionTypeDto;
import io.vestoria.dto.response.BuildingResponseDto;
import io.vestoria.dto.response.ItemResponseDto;
import io.vestoria.entity.BuildingEntity;
import io.vestoria.entity.ItemEntity;
import io.vestoria.enums.BuildingSubType;
import io.vestoria.enums.BuildingTier;
import io.vestoria.enums.BuildingType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class BuildingConverter {

    public BuildingProductionTypeDto toProductionTypeDto(BuildingSubType subType) {
        return BuildingProductionTypeDto.builder()
                .value(subType.name())
                .label(subType.getLabel())
                .description(subType.getDescription())
                .parentType(subType.getParentType())
                .build();
    }

    public BuildingConfigDto toConfigDto(BuildingType type, BuildingTier tier, BigDecimal cost,
            BigDecimal productionRate, Integer maxStock, Integer maxSlots) {
        return BuildingConfigDto.builder()
                .type(type)
                .tier(tier)
                .cost(cost)
                .productionRate(productionRate)
                .maxStock(maxStock)
                .maxSlots(maxSlots)
                .build();
    }

    public BuildingResponseDto toResponseDto(BuildingEntity entity) {
        return BuildingResponseDto.builder()
                .id(entity.getId())
                .type(entity.getType())
                .tier(entity.getTier())
                .subType(entity.getSubType())
                .level(entity.getLevel())
                .productionRate(entity.getProductionRate())
                .maxStock(entity.getMaxStock())
                .maxSlots(entity.getMaxSlots())
                .cost(entity.getCost())
                .status(entity.getStatus())
                .productionEndsAt(entity.getProductionEndsAt())
                .salesEndsAt(entity.getSalesEndsAt())
                .isProducing(entity.getIsProducing())
                .isSelling(entity.getIsSelling())
                .currentStock(entity.getItems() != null
                        ? entity.getItems().stream().mapToInt(ItemEntity::getQuantity).sum()
                        : 0)
                .items(entity.getItems() != null
                        ? entity.getItems().stream().map(this::toItemResponseDto)
                                .collect(Collectors.toList())
                        : Collections.emptyList())
                .lastRevenue(entity.getLastRevenue())
                .build();
    }

    private ItemResponseDto toItemResponseDto(ItemEntity item) {
        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .unit(item.getUnit())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .qualityScore(item.getQualityScore())
                .tier(item.getTier())
                .category(item.getCategory())
                .build();
    }
}
