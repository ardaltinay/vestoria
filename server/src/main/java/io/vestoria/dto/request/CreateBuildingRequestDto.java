package io.vestoria.dto.request;

import io.vestoria.enums.BuildingTier;
import io.vestoria.enums.BuildingType;
import io.vestoria.enums.BuildingSubType;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class CreateBuildingRequestDto implements Serializable {
  private String name;
  private BuildingType type;
  private BuildingTier tier;
  private BuildingSubType subType;
  private Integer level;
  private UUID ownerId;
}
