import 'package:flutter/material.dart';

/// Centralized building icons for consistent UI across the app.
/// All building icons should reference this class to ensure consistency.
class BuildingIcons {
  // Prevent instantiation
  BuildingIcons._();

  // ============ EMOJIS (PREFERRED) ============
  static const String shopEmoji = '🏪';      // Shopping cart - cleaner
  static const String farmEmoji = '🐄';      // Seedling - fresh/growth
  static const String factoryEmoji = '⚙️';   // Gear - industrial
  static const String mineEmoji = '⛏️';       // Gem - valuable resources
  static const String gardenEmoji = '🌳';    // Hibiscus - beautiful flower

  /// Get emoji for building type
  static String getEmoji(String buildingType) {
    switch (buildingType.toUpperCase()) {
      case 'SHOP':
        return shopEmoji;
      case 'FARM':
        return farmEmoji;
      case 'FACTORY':
        return factoryEmoji;
      case 'MINE':
        return mineEmoji;
      case 'GARDEN':
        return gardenEmoji;
      default:
        return '🏢';
    }
  }

  // ============ MATERIAL ICONS (FALLBACK) ============
  // Shops
  static const IconData shop = Icons.storefront;
  static const IconData shopOutlined = Icons.storefront_outlined;
  
  // Farms
  static const IconData farm = Icons.agriculture;
  static const IconData farmOutlined = Icons.agriculture_outlined;
  
  // Factories
  static const IconData factory = Icons.factory;
  static const IconData factoryOutlined = Icons.factory_outlined;
  
  // Mines
  static const IconData mine = Icons.hardware;
  static const IconData mineOutlined = Icons.hardware_outlined;
  
  // Gardens
  static const IconData garden = Icons.park;
  static const IconData gardenOutlined = Icons.park_outlined;

  /// Get icon for building type (filled version)
  static IconData getIcon(String buildingType) {
    switch (buildingType.toUpperCase()) {
      case 'SHOP':
        return shop;
      case 'FARM':
        return farm;
      case 'FACTORY':
        return factory;
      case 'MINE':
        return mine;
      case 'GARDEN':
        return garden;
      default:
        return Icons.business;
    }
  }

  /// Get outlined icon for building type
  static IconData getOutlinedIcon(String buildingType) {
    switch (buildingType.toUpperCase()) {
      case 'SHOP':
        return shopOutlined;
      case 'FARM':
        return farmOutlined;
      case 'FACTORY':
        return factoryOutlined;
      case 'MINE':
        return mineOutlined;
      case 'GARDEN':
        return gardenOutlined;
      default:
        return Icons.business_outlined;
    }
  }

  /// Get default color for building type
  static Color getColor(String buildingType) {
    switch (buildingType.toUpperCase()) {
      case 'SHOP':
        return const Color(0xFF6366F1); // Indigo
      case 'FARM':
        return const Color(0xFF22C55E); // Green
      case 'FACTORY':
        return const Color(0xFF3B82F6); // Blue
      case 'MINE':
        return const Color(0xFFA855F7); // Purple
      case 'GARDEN':
        return const Color(0xFFF472B6); // Pink
      default:
        return const Color(0xFF64748B); // Slate
    }
  }
}

