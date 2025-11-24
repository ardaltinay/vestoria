package io.vestoria.dto.response;

import io.vestoria.enums.BuildingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildingProductionTypeDto implements Serializable {
  private String value;
  private String label;
  private String description;
  private BuildingType parentType;
}
