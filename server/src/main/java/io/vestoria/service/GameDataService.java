package io.vestoria.service;

import io.vestoria.constant.Constants;
import io.vestoria.dto.response.ItemDefinition;
import io.vestoria.enums.BuildingSubType;
import io.vestoria.enums.BuildingType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class GameDataService {

    @Cacheable("gameDataServiceResult")
    public List<ItemDefinition> getAllItems() {
        List<ItemDefinition> items = new ArrayList<>();

        for (BuildingSubType subType : BuildingSubType.values()) {
            if (subType == BuildingSubType.GENERIC)
                continue;

            if (BuildingType.SHOP.equals(subType.getParentType())) {
                items.add(ItemDefinition.builder().id(subType.name()) // e.g., "MARKET"
                        .name(subType.getLabel()) // e.g., "Market"
                        .label(subType.getLabel()).description(subType.getDescription()).type(subType.getParentType())
                        .allowedItems(subType.getMarketableProducts()).build());
                continue;
            }

            if (subType == BuildingSubType.FACTORY) {
                // Collect all raw materials
                List<String> rawMaterials = Constants.FACTORY_MAP.values().stream().flatMap(List::stream).distinct()
                        .toList();

                // Collect all factory products (keys of FACTORY_MAP)
                List<String> factoryProducts = new ArrayList<>(Constants.FACTORY_MAP.keySet());

                items.add(ItemDefinition.builder().id(subType.name()) // "FACTORY"
                        .name(subType.getLabel()) // "Fabrika"
                        .label(subType.getLabel()).description(subType.getDescription()).type(subType.getParentType())
                        .rawMaterials(rawMaterials)
                        .producedItemNames(factoryProducts) // Add factory products for frontend
                        .build());
            }

            if (subType.getProducedItemNames() != null && !subType.getProducedItemNames().isEmpty()) {
                // Add the building definition itself (e.g. FARM, MINE, GARDEN)
                // This allows frontend to look up the building type and see what it produces
                if (items.stream().noneMatch(i -> i.getId().equals(subType.name()))) {
                    items.add(ItemDefinition.builder().id(subType.name()).name(subType.getLabel())
                            .label(subType.getLabel()).description(subType.getDescription())
                            .type(subType.getParentType()).producedItemNames(subType.getProducedItemNames()).build());
                }

                // Add individual items (for display purposes if needed)
                subType.getProducedItemNames().forEach(productName -> {
                    items.add(ItemDefinition.builder().id(productName) // e.g., "Buğday", "Domates"
                            .name(productName) // e.g., "Buğday", "Domates"
                            .label(subType.getLabel()) // e.g., "Çiftlik", "Bahçe"
                            .description(subType.getDescription()).type(subType.getParentType()).build());
                });
            }
        }

        return items;
    }
}
