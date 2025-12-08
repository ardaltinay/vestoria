package io.vestoria.dto.response;

import io.vestoria.enums.BuildingType;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
