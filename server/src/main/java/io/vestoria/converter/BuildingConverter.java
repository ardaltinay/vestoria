package io.vestoria.converter;

import io.vestoria.dto.response.CreateBuildingResponseDto;
import io.vestoria.entity.BuildingEntity;
import org.springframework.stereotype.Component;

@Component
public class BuildingConverter {

    public CreateBuildingResponseDto toDto(BuildingEntity entity) {
        return CreateBuildingResponseDto.builder()
                .id(entity.getId().toString())
                .level(entity.getLevel())
                .cost(entity.getCost())
                .type(entity.getType().name())
                .tier(entity.getTier() != null ? entity.getTier().name() : null)
                .maxStock(entity.getMaxStock())
                .status(entity.getStatus().name())
                .productionRate(entity.getProductionRate())
                .ownerId(entity.getOwner().getId().toString())
                .build();
    }
}
