package io.vestoria.enums;

import lombok.Getter;

@Getter
public enum BuildingSubType {
  // Shops
  MARKET(BuildingType.SHOP, null, "Market", "Temel gıda ve ihtiyaç malzemeleri."),
  CLOTHING(BuildingType.SHOP, null, "Giyim Mağazası", "Moda ve tekstil ürünleri."),
  GREENGROCER(BuildingType.SHOP, null, "Manav", "Taze meyve ve sebze."),
  JEWELER(BuildingType.SHOP, null, "Kuyumcu", "Değerli takı ve aksesuarlar."),

  // Gardens
  VEGETABLE_GARDEN(BuildingType.GARDEN, "Domates", "Sebze Bahçesi", "Mevsimlik sebze yetiştiriciliği."),
  FRUIT_ORCHARD(BuildingType.GARDEN, "Elma", "Meyve Bahçesi", "Ağaç meyveleri üretimi."),

  // Farms
  LIVESTOCK(BuildingType.FARM, "Süt", "Hayvancılık", "Et ve süt ürünleri üretimi."),
  WHEAT_FIELD(BuildingType.FARM, "Buğday", "Buğday Tarlası", "Temel tahıl üretimi."),

  // Factories
  TEXTILE(BuildingType.FACTORY, "Ceket", "Tekstil Fabrikası", "Kumaş ve giyim üretimi."),
  STEEL_FACTORY(BuildingType.FACTORY, "Çelik", "Çelik Fabrikası", "Metal işleme ve üretim."),

  // Mines
  IRON_MINE(BuildingType.MINE, "Demir", "Demir Madeni", "Demir cevheri çıkarımı."),
  COAL_MINE(BuildingType.MINE, "Kömür", "Kömür Madeni", "Enerji için kömür çıkarımı."),
  GOLD_MINE(BuildingType.MINE, "Altın", "Altın Madeni", "Değerli metal madenciliği."),
  COPPER_MINE(BuildingType.MINE, "Bakır", "Bakır Madeni", "Endüstriyel metal madenciliği."),
  OIL_WELL(BuildingType.MINE, "Petrol", "Petrol Kuyusu", "Enerji üretimi."),

  // Generic/Fallback
  GENERIC(null, null, "Standart", "Standart üretim tesisi.");

  private final BuildingType parentType;
  private final String producedItemName;
  private final String label;
  private final String description;

  BuildingSubType(BuildingType parentType, String producedItemName, String label, String description) {
    this.parentType = parentType;
    this.producedItemName = producedItemName;
    this.label = label;
    this.description = description;
  }
}
