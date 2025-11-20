package io.vestoria.enums;

import lombok.Getter;

@Getter
public enum BuildingSubType {
  // Shops
  MARKET(BuildingType.SHOP, null),
  CLOTHING(BuildingType.SHOP, null),
  GREENGROCER(BuildingType.SHOP, null),
  JEWELER(BuildingType.SHOP, null),

  // Gardens
  VEGETABLE_GARDEN(BuildingType.GARDEN, "Domates"),
  FRUIT_ORCHARD(BuildingType.GARDEN, "Elma"),

  // Farms
  LIVESTOCK(BuildingType.FARM, "Süt"),
  WHEAT_FIELD(BuildingType.FARM, "Buğday"),

  // Factories
  TEXTILE(BuildingType.FACTORY, "Ceket"),
  STEEL_FACTORY(BuildingType.FACTORY, "Çelik"),

  // Mines
  IRON_MINE(BuildingType.MINE, "Demir"),
  COAL_MINE(BuildingType.MINE, "Kömür"),
  GOLD_MINE(BuildingType.MINE, "Altın"),
  COPPER_MINE(BuildingType.MINE, "Bakır"),
  OIL_WELL(BuildingType.MINE, "Petrol"),

  // Generic/Fallback
  GENERIC(null, null);

  private final BuildingType parentType;
  private final String producedItemName;

  BuildingSubType(BuildingType parentType, String producedItemName) {
    this.parentType = parentType;
    this.producedItemName = producedItemName;
  }
}
