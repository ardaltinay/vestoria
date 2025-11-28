package io.vestoria.enums;

import io.vestoria.constant.Constants;
import lombok.Getter;

import java.util.List;

@Getter
public enum BuildingSubType {
  // Shops (they sell, not produce)
  MARKET(BuildingType.SHOP, null, Constants.MARKET_ITEMS, "Market", "Temel gıda ve ihtiyaç malzemeleri."),
  CLOTHING(BuildingType.SHOP, null, Constants.CLOTHING_ITEMS, "Giyim Mağazası", "Moda ve tekstil ürünleri."),
  GREENGROCER(BuildingType.SHOP, null, Constants.GREENGROCER_ITEMS, "Manav", "Taze meyve ve sebze."),
  JEWELER(BuildingType.SHOP, null, Constants.JEWELER_ITEMS, "Kuyumcu", "Değerli takı ve aksesuarlar."),

  // Gardens
  GARDEN(BuildingType.GARDEN, Constants.GARDEN_ITEMS, null, "Bahçe", "Meyve ve Sebze Bahçeleri"),

  // Farms
  FARM(BuildingType.FARM, Constants.FARM_ITEMS, null, "Çiftlik", "Tarım ve Hayvan Çiftlikleri"),

  // Factories
  FACTORY(BuildingType.FACTORY, Constants.FACTORY_MAP.keySet().stream().toList(), null, "Fabrika", "Fabrikalar"),

  // Mines
  MINE(BuildingType.MINE, Constants.MINE_ITEMS, null, "Maden", "Madenler"),

  // Generic/Fallback
  GENERIC(null, null, null, "Standart", "Standart üretim tesisi.");

  private final BuildingType parentType;
  private final List<String> producedItemNames;
  private final List<String> marketableProducts;
  private final String label;
  private final String description;

  BuildingSubType(BuildingType parentType, List<String> producedItemNames, List<String> marketableProducts, String label,
      String description) {
    this.parentType = parentType;
    this.producedItemNames = producedItemNames;
    this.marketableProducts = marketableProducts;
    this.label = label;
    this.description = description;
  }
}
