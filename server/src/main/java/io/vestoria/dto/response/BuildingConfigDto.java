package io.vestoria.dto.response;

import io.vestoria.enums.BuildingTier;
import io.vestoria.enums.BuildingType;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BuildingConfigDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private BuildingType type;
    private BuildingTier tier;
    private BigDecimal cost;
    private BigDecimal productionRate;
    private float productionDuration;
    private float salesDuration;
    private Integer maxStock;
    private Integer maxSlots;
}
