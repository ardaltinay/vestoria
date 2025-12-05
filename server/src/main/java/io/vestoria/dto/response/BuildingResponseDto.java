package io.vestoria.dto.response;

import io.vestoria.enums.BuildingStatus;
import io.vestoria.enums.BuildingSubType;
import io.vestoria.enums.BuildingTier;
import io.vestoria.enums.BuildingType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BuildingResponseDto implements Serializable {
    private UUID id;
    private String name;
    private String label;
    private String description;
    private BuildingType type;
    private BuildingTier tier;
    private BuildingSubType subType;
    private BigDecimal productionRate;
    private Integer maxStock;
    private Integer maxSlots;
    private BigDecimal cost;
    private BuildingStatus status;
    private LocalDateTime productionEndsAt;
    private LocalDateTime salesEndsAt;
    private Boolean isProducing;
    private Boolean isSelling;
    private Integer currentStock;
    private List<ItemResponseDto> items;
    private BigDecimal lastRevenue;
}
