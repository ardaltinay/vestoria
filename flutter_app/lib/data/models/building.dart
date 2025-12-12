import 'package:equatable/equatable.dart';

/// Represents an item in a building's inventory
class BuildingItem extends Equatable {
  final String id;
  final String name;
  final int quantity;
  final String? unit;
  final double qualityScore;
  final double? price;
  final double? cost;
  final String? tier;

  const BuildingItem({
    required this.id,
    required this.name,
    required this.quantity,
    this.unit,
    this.qualityScore = 0,
    this.price,
    this.cost,
    this.tier,
  });

  factory BuildingItem.fromJson(Map<String, dynamic> json) {
    return BuildingItem(
      id: json['id']?.toString() ?? '',
      name: json['name'] ?? '',
      quantity: json['quantity'] ?? 0,
      unit: json['unit'],
      qualityScore: (json['qualityScore'] ?? 0).toDouble(),
      price: json['price']?.toDouble(),
      cost: json['cost']?.toDouble(),
      tier: json['tier'],
    );
  }

  @override
  List<Object?> get props => [id, name, quantity];
}

/// Generic building model that works for all building types (shops, farms, factories, mines, gardens)
class Building extends Equatable {
  final String id;
  final String name;
  final String? description;
  final String type; // SHOP, FARM, FACTORY, MINE, GARDEN
  final String? subType; // BAKKAL, MANAV, etc.
  final String tier; // SMALL, MEDIUM, LARGE
  final String? status; // ACTIVE, INACTIVE, etc.
  final bool isActive;
  final int? currentStock;
  final int? maxStock;
  final int? maxSlots;
  final double? cost;
  final double? productionRate;
  final double? lastRevenue;
  final bool isProducing;
  final bool isSelling;
  final DateTime? productionEndsAt;
  final DateTime? salesEndsAt;
  final DateTime? createdAt;
  final List<BuildingItem> items;

  const Building({
    required this.id,
    required this.name,
    this.description,
    required this.type,
    this.subType,
    this.tier = 'SMALL',
    this.status,
    this.isActive = true,
    this.currentStock,
    this.maxStock,
    this.maxSlots,
    this.cost,
    this.productionRate,
    this.lastRevenue,
    this.isProducing = false,
    this.isSelling = false,
    this.productionEndsAt,
    this.salesEndsAt,
    this.createdAt,
    this.items = const [],
  });

  factory Building.fromJson(Map<String, dynamic> json) {
    // Parse items list
    List<BuildingItem> itemsList = [];
    if (json['items'] != null && json['items'] is List) {
      itemsList = (json['items'] as List)
          .map((item) => BuildingItem.fromJson(Map<String, dynamic>.from(item)))
          .toList();
    }

    // Calculate current stock from items
    int calculatedStock = itemsList.fold(0, (sum, item) => sum + item.quantity);

    return Building(
      id: json['id']?.toString() ?? '',
      name: json['name'] ?? '',
      description: json['description'],
      type: json['type'] ?? json['buildingType'] ?? 'SHOP',
      subType: json['subType'],
      tier: json['tier'] ?? 'SMALL',
      status: json['status'],
      isActive: json['isActive'] ?? json['active'] ?? true,
      currentStock: json['currentStock'] ?? calculatedStock,
      maxStock: json['maxStock'],
      maxSlots: json['maxSlots'],
      cost: json['cost']?.toDouble(),
      productionRate: json['productionRate']?.toDouble(),
      lastRevenue: json['lastRevenue']?.toDouble(),
      isProducing: json['isProducing'] ?? false,
      isSelling: json['isSelling'] ?? false,
      productionEndsAt: json['productionEndsAt'] != null
          ? DateTime.tryParse(json['productionEndsAt'].toString())
          : null,
      salesEndsAt: json['salesEndsAt'] != null
          ? DateTime.tryParse(json['salesEndsAt'].toString())
          : null,
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'].toString())
          : null,
      items: itemsList,
    );
  }

  /// Stock percentage for progress bar (0.0 - 1.0)
  double get stockPercentage {
    if (currentStock == null || maxStock == null || maxStock == 0) return 0;
    return (currentStock! / maxStock!).clamp(0.0, 1.0);
  }

  /// Stock text display (e.g., "5/100")
  String get stockText {
    if (currentStock == null || maxStock == null) return '-';
    return '$currentStock/$maxStock';
  }

  /// Tier level number (1, 2, 3)
  int get tierLevel {
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

  /// Tier display text
  String get tierText {
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

  /// Subtype Turkish translation
  String get subTypeTr {
    if (subType == null) return '';
    switch (subType!.toUpperCase()) {
      // Shop types
      case 'BAKKAL':
        return 'Bakkal';
      case 'MANAV':
        return 'Manav';
      case 'KASAP':
        return 'Kasap';
      case 'FIRINCI':
        return 'Fırıncı';
      case 'MARKET':
        return 'Market';
      // Farm types
      case 'TAHIL':
        return 'Tahıl Çiftliği';
      case 'SEBZE':
        return 'Sebze Çiftliği';
      case 'MEYVE':
        return 'Meyve Bahçesi';
      case 'HAYVANCILIK':
        return 'Hayvancılık';
      // Factory types
      case 'GIDA':
        return 'Gıda Fabrikası';
      case 'TEKSTIL':
        return 'Tekstil Fabrikası';
      case 'ELEKTRONIK':
        return 'Elektronik Fabrikası';
      // Mine types
      case 'KOMUR':
        return 'Kömür Madeni';
      case 'DEMIR':
        return 'Demir Madeni';
      case 'ALTIN':
        return 'Altın Madeni';
      // Garden types
      case 'CICEK':
        return 'Çiçek Bahçesi';
      case 'SERA':
        return 'Sera';
      default:
        return subType!;
    }
  }

  @override
  List<Object?> get props => [id, name, type, tier, isActive];
}
