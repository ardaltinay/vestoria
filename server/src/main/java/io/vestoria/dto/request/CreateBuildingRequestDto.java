package io.vestoria.dto.request;

import io.vestoria.enums.BuildingSubType;
import io.vestoria.enums.BuildingTier;
import io.vestoria.enums.BuildingType;
import java.io.Serializable;
import java.util.UUID;
import lombok.Data;

@Data
public class CreateBuildingRequestDto implements Serializable {
    private String name;
    private BuildingType type;
    private BuildingTier tier;
    private BuildingSubType subType;
    private Integer level;
    private UUID ownerId;
}
