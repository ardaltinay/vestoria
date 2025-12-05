package io.vestoria.dto.response;

import io.vestoria.enums.BuildingType;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDefinition implements Serializable {
    private String id;
    private String name;
    private String label;
    private String description;
    private BuildingType type;
    private List<String> allowedItems;
    private List<String> rawMaterials;
    private List<String> producedItemNames;
}
