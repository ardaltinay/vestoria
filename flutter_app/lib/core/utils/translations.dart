/// Turkish translations for building types and subtypes
/// Matches Vue.js /utils/translations.js

class Translations {
  /// Translate building type to Turkish
  static String getBuildingTypeTr(String? type) {
    if (type == null) return '';
    switch (type.toUpperCase()) {
      case 'SHOP':
        return 'Dükkan';
      case 'FARM':
        return 'Çiftlik';
      case 'MINE':
        return 'Maden';
      case 'FACTORY':
        return 'Fabrika';
      case 'GARDEN':
        return 'Bahçe';
      default:
        return type;
    }
  }

  /// Translate building subtype to Turkish
  static String getBuildingSubTypeTr(String? subType) {
    if (subType == null) return '';
    switch (subType.toUpperCase()) {
      // Shops
      case 'MARKET':
        return 'Market';
      case 'GREENGROCER':
        return 'Manav';
      case 'CLOTHING':
        return 'Giyim';
      case 'JEWELER':
        return 'Kuyumcu';
      case 'BUTCHER':
        return 'Kasap';
      case 'BAKERY':
        return 'Fırın';
      case 'ELECTRONICS':
        return 'Elektronik';
      case 'FURNITURE':
        return 'Mobilya';
      case 'HARDWARE':
        return 'Nalburiye';
      case 'PHARMACY':
        return 'Eczane';
      
      // Production buildings
      case 'GARDEN':
        return 'Bahçe';
      case 'FARM':
        return 'Çiftlik';
      case 'FACTORY':
        return 'Fabrika';
      case 'MINE':
        return 'Maden';
      
      // Farm subtypes
      case 'DAIRY':
        return 'Mandıra';
      case 'POULTRY':
        return 'Kümes';
      case 'WHEAT':
        return 'Buğday';
      case 'CORN':
        return 'Mısır';
      case 'CATTLE':
        return 'Büyükbaş';
      case 'SHEEP':
        return 'Küçükbaş';
      
      // Factory subtypes
      case 'FOOD':
        return 'Gıda';
      case 'TEXTILE':
        return 'Tekstil';
      case 'STEEL':
        return 'Çelik';
      case 'CHEMICAL':
        return 'Kimya';
      case 'AUTOMOTIVE':
        return 'Otomotiv';
      
      // Mine subtypes
      case 'COAL':
        return 'Kömür';
      case 'IRON':
        return 'Demir';
      case 'GOLD':
        return 'Altın';
      case 'COPPER':
        return 'Bakır';
      case 'SILVER':
        return 'Gümüş';
      
      // Garden subtypes
      case 'VEGETABLE':
        return 'Sebze';
      case 'FRUIT':
        return 'Meyve';
      case 'FLOWER':
        return 'Çiçek';
      case 'HERB':
        return 'Bitki';
      
      default:
        return subType;
    }
  }

  /// Translate item unit to Turkish
  static String getItemUnitTr(String? unit) {
    if (unit == null) return '';
    switch (unit.toUpperCase()) {
      case 'PIECE':
        return 'Adet';
      case 'KG':
        return 'Kg';
      case 'LITER':
        return 'Litre';
      case 'METER':
        return 'Metre';
      case 'TON':
        return 'Ton';
      case 'GRAM':
        return 'Gram';
      default:
        return unit;
    }
  }

  /// Translate tier to Turkish
  static String getTierTr(String? tier) {
    if (tier == null) return '';
    switch (tier.toUpperCase()) {
      case 'SMALL':
        return 'Küçük';
      case 'MEDIUM':
        return 'Orta';
      case 'LARGE':
        return 'Büyük';
      default:
        return tier;
    }
  }

  /// Get tier level number
  static int getTierLevel(String? tier) {
    if (tier == null) return 1;
    switch (tier.toUpperCase()) {
      case 'SMALL':
        return 1;
      case 'MEDIUM':
        return 2;
      case 'LARGE':
        return 3;
      default:
        return 1;
    }
  }

  /// Get building detail page title
  static String getDetailPageTitle(String? type) {
    if (type == null) return 'İşletme Detayı';
    switch (type.toUpperCase()) {
      case 'SHOP':
        return 'Dükkan Detayı';
      case 'FARM':
        return 'Çiftlik Detayı';
      case 'MINE':
        return 'Maden Detayı';
      case 'FACTORY':
        return 'Fabrika Detayı';
      case 'GARDEN':
        return 'Bahçe Detayı';
      default:
        return 'İşletme Detayı';
    }
  }

  /// Check if building type is production (not shop)
  static bool isProductionBuilding(String? type) {
    if (type == null) return false;
    return ['FARM', 'FACTORY', 'MINE', 'GARDEN'].contains(type.toUpperCase());
  }
}
