package io.vestoria.model;

import io.vestoria.enums.BuildingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

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
