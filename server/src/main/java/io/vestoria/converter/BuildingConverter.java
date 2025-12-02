package io.vestoria.converter;

import io.vestoria.dto.response.BuildingConfigDto;
import io.vestoria.dto.response.BuildingProductionTypeDto;
import io.vestoria.dto.response.BuildingResponseDto;
import io.vestoria.entity.BuildingEntity;
import io.vestoria.entity.ItemEntity;
import io.vestoria.enums.BuildingSubType;
import io.vestoria.enums.BuildingTier;
import io.vestoria.enums.BuildingType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BuildingConverter {

        private final ItemConverter itemConverter;

        public BuildingProductionTypeDto toProductionTypeDto(BuildingSubType subType) {
                return BuildingProductionTypeDto.builder()
                                .value(subType.name())
                                .label(subType.getLabel())
                                .description(subType.getDescription())
                                .parentType(subType.getParentType())
                                .build();
        }

        public BuildingConfigDto toConfigDto(BuildingType type, BuildingTier tier, BigDecimal cost,
                        BigDecimal productionRate, float productionDuration, float salesDuration, Integer maxStock,
                        Integer maxSlots) {
                return BuildingConfigDto.builder()
                                .type(type)
                                .tier(tier)
                                .cost(cost)
                                .productionRate(productionRate)
                                .productionDuration(productionDuration)
                                .salesDuration(salesDuration)
                                .maxStock(maxStock)
                                .maxSlots(maxSlots)
                                .build();
        }

        public BuildingResponseDto toResponseDto(BuildingEntity entity) {
                // Determine label and description
                String label;
                String description;

                if (entity.getSubType() != null) {
                        label = entity.getSubType().getLabel();
                        description = entity.getSubType().getDescription();
                } else {
                        // Fallback to type-based labels for non-SHOP buildings
                        label = switch (entity.getType()) {
                                case GARDEN -> "Bahçe";
                                case FARM -> "Çiftlik";
                                case FACTORY -> "Fabrika";
                                case MINE -> "Maden";
                                default -> entity.getType().name();
                        };
                        description = switch (entity.getType()) {
                                case GARDEN -> "Sebze ve meyve yetiştirme alanı";
                                case FARM -> "Hayvansal ve tarımsal ürün üretimi";
                                case FACTORY -> "İleri seviye ürün üretimi";
                                case MINE -> "Değerli madenler ve kaynaklar";
                                default -> "";
                        };
                }

                return BuildingResponseDto.builder()
                                .id(entity.getId())
                                .name(entity.getName())
                                .label(label)
                                .description(description)
                                .type(entity.getType())
                                .tier(entity.getTier())
                                .subType(entity.getSubType())
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
                                                ? entity.getItems().stream().map(itemConverter::toItemResponseDto)
                                                                .collect(Collectors.toList())
                                                : Collections.emptyList())
                                .lastRevenue(entity.getLastRevenue())
                                .build();
        }
}
