package io.vestoria.service;

import io.vestoria.constant.Constants;
import io.vestoria.enums.BuildingSubType;
import io.vestoria.enums.BuildingType;
import io.vestoria.model.ItemDefinition;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameDataService {

  @Cacheable("gameDataServiceResult")
  public List<ItemDefinition> getAllItems() {
    List<ItemDefinition> items = new ArrayList<>();

    for (BuildingSubType subType : BuildingSubType.values()) {
      if (subType == BuildingSubType.GENERIC)
        continue;

      if (BuildingType.SHOP.equals(subType.getParentType())) {
        items.add(ItemDefinition.builder()
            .id(subType.name()) // e.g., "MARKET"
            .name(subType.getLabel()) // e.g., "Market"
            .label(subType.getLabel())
            .description(subType.getDescription())
            .type(subType.getParentType())
            .allowedItems(subType.getMarketableProducts())
            .build());
        continue;
      }

      if (subType == BuildingSubType.FACTORY) {
        // Collect all raw materials
        List<String> rawMaterials = Constants.FACTORY_MAP.values().stream()
            .flatMap(List::stream)
            .distinct()
            .toList();

        items.add(ItemDefinition.builder()
            .id(subType.name()) // "FACTORY"
            .name(subType.getLabel()) // "Fabrika"
            .label(subType.getLabel())
            .description(subType.getDescription())
            .type(subType.getParentType())
            .rawMaterials(rawMaterials)
            .build());
      }

      if (subType.getProducedItemName() != null && !subType.getProducedItemName().isEmpty()) {
        subType.getProducedItemName().forEach(productName -> {
          items.add(ItemDefinition.builder()
              .id(productName) // e.g., "Buğday", "Domates"
              .name(productName) // e.g., "Buğday", "Domates"
              .label(subType.getLabel()) // e.g., "Çiftlik", "Bahçe"
              .description(subType.getDescription())
              .type(subType.getParentType())
              .build());
        });
      }
    }

    return items;
  }
}
