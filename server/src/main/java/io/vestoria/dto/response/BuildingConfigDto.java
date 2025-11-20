package io.vestoria.dto.response;

import io.vestoria.enums.BuildingTier;
import io.vestoria.enums.BuildingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BuildingConfigDto {
  private BuildingType type;
  private BuildingTier tier;
  private BigDecimal cost;
  private BigDecimal productionRate;
  private Integer maxStock;
  private Integer maxSlots;
}
