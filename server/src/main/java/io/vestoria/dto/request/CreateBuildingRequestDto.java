package io.vestoria.dto.request;

import io.vestoria.enums.BuildingType;

public record CreateBuildingRequestDto(BuildingType type, String subType, Integer level, String ownerId) {
}
