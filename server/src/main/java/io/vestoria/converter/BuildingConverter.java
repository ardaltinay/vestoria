package io.vestoria.converter;

import io.vestoria.dto.response.BuildingConfigDto;
import io.vestoria.dto.response.BuildingProductionTypeDto;
import io.vestoria.enums.BuildingSubType;
import io.vestoria.enums.BuildingTier;
import io.vestoria.enums.BuildingType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

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
}
