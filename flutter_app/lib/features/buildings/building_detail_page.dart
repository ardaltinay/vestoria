import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'dart:async';
import '../../core/theme/app_theme.dart';
import '../../core/api/api_client.dart';
import '../../data/models/building.dart';
import '../../core/widgets/currency_icon.dart';
import '../../core/widgets/product_emoji.dart';
import '../../core/widgets/building_icons.dart';
import '../../core/utils/translations.dart';

// Building detail provider - fetches list and filters by ID
final buildingDetailProvider = FutureProvider.family<Building?, String>((ref, id) async {
  final api = ApiClient();
  try {
    final response = await api.getAllBuildings();
    final List allData = response.data ?? [];
    final buildingData = allData.firstWhere(
      (b) => b['id']?.toString() == id,
      orElse: () => null,
    );
    if (buildingData == null) {
      print('Building not found with ID: $id');
      return null;
    }
    return Building.fromJson(Map<String, dynamic>.from(buildingData));
  } catch (e) {
    print('Building detail error: $e');
    return null;
  }
});

class BuildingDetailPage extends ConsumerStatefulWidget {
  final String buildingId;
  final String buildingType;
  
  const BuildingDetailPage({
    super.key, 
    required this.buildingId,
    required this.buildingType,
  });

  @override
  ConsumerState<BuildingDetailPage> createState() => _BuildingDetailPageState();
}

class _BuildingDetailPageState extends ConsumerState<BuildingDetailPage> {
  bool _isLoading = false;
  Timer? _timer;
  Duration _remainingTime = Duration.zero;

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  void _startTimer(DateTime endTime) {
    _timer?.cancel();
    _updateRemainingTime(endTime);
    _timer = Timer.periodic(const Duration(seconds: 1), (_) {
      _updateRemainingTime(endTime);
    });
  }

  void _updateRemainingTime(DateTime endTime) {
    final now = DateTime.now();
    final remaining = endTime.difference(now);
    if (remaining.isNegative) {
      _timer?.cancel();
      setState(() => _remainingTime = Duration.zero);
      // Don't invalidate provider here - just show "Tamamlandı" state
      // User will click the button to complete the sale/production
    } else {
      setState(() => _remainingTime = remaining);
    }
  }

  String _formatDuration(Duration d) {
    final minutes = d.inMinutes;
    final seconds = d.inSeconds % 60;
    return '${minutes.toString().padLeft(2, '0')}:${seconds.toString().padLeft(2, '0')}';
  }

  bool get isShop => widget.buildingType.toUpperCase() == 'SHOP';
  bool get isProductionBuilding => Translations.isProductionBuilding(widget.buildingType);

  Future<void> _handleStartSales(Building building) async {
    setState(() => _isLoading = true);
    try {
      final api = ApiClient();
      await api.startSales(building.id);
      ref.invalidate(buildingDetailProvider(widget.buildingId));
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Satış işlemi başladı!'), backgroundColor: Colors.green),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(ApiClient.getErrorMessage(e)), backgroundColor: Colors.red),
        );
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  Future<void> _handleCompleteSale(Building building) async {
    setState(() => _isLoading = true);
    try {
      final api = ApiClient();
      await api.completeSale(building.id);
      ref.invalidate(buildingDetailProvider(widget.buildingId));
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Satış tamamlandı!'), backgroundColor: Colors.green),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(ApiClient.getErrorMessage(e)), backgroundColor: Colors.red),
        );
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  Future<void> _handleStartProduction(Building building) async {
    // Load game data to get producible items for this building type
    List<Map<String, dynamic>> products = [];
    int? productionDuration;
    
    try {
      final api = ApiClient();
      
      // Fetch production duration from backend config FIRST
      try {
        final configResponse = await api.getBuildingConfigs();
        final List configs = configResponse.data ?? [];
        
        // Debug: print building type and tier
        print('Looking for config: type=${building.type}, tier=${building.tier}');
        print('Available configs count: ${configs.length}');
        if (configs.isNotEmpty) {
          print('First config sample: ${configs.first}');
        }
        
        // Find matching config (case-insensitive comparison)
        final config = configs.firstWhere(
          (c) => (c['buildingType']?.toString().toUpperCase() == building.type.toUpperCase()) &&
                 (c['tier']?.toString().toUpperCase() == building.tier?.toUpperCase()),
          orElse: () => null,
        );
        
        print('Found config: $config');
        
        if (config != null) {
          final duration = config['productionDuration'];
          print('Production duration from config: $duration (type: ${duration.runtimeType})');
          if (duration != null) {
            productionDuration = (duration as num).toInt();
          }
        }
      } catch (e) {
        print('Error loading building config: $e');
      }
      
      // No default fallback - only show backend value
      print('Final productionDuration: $productionDuration');
      
      // Get game data items for product options
      final gameDataResponse = await api.getGameData();
      final List gameData = gameDataResponse.data ?? [];
      
      // Find the definition for this building's subType
      final subType = building.subType ?? building.type;
      final definition = gameData.firstWhere(
        (d) => d['id'] == subType,
        orElse: () => null,
      );
      
      if (definition != null) {
        // Get produced item names
        final List producedItems = definition['producedItemNames'] ?? [];
        products = producedItems.map<Map<String, dynamic>>((name) => {
          'id': name.toString(),
          'name': name.toString(),
        }).toList();
      }
    } catch (e) {
      print('Error loading production data: $e');
    }
    
    if (products.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Üretilecek ürün bulunamadı'), backgroundColor: Colors.orange),
      );
      return;
    }
    
    String? selectedProduct = products.first['id'];
    
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setDialogState) => AlertDialog(
          title: Row(
            children: [
              Icon(Icons.settings, color: AppColors.primary),
              const SizedBox(width: 8),
              const Text('Üretim Emri'),
            ],
          ),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Product Selection
              Text('Üretilecek Mahsul', style: TextStyle(fontWeight: FontWeight.w600, color: AppColors.slate600)),
              const SizedBox(height: 8),
              DropdownButtonFormField<String>(
                value: selectedProduct,
                decoration: InputDecoration(
                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
                  contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                ),
                items: products.map((p) => DropdownMenuItem(
                  value: p['id']?.toString(),
                  child: Row(
                    children: [
                      ProductEmoji(productName: p['name']?.toString() ?? '', size: 20),
                      const SizedBox(width: 8),
                      Text(p['name']?.toString() ?? ''),
                    ],
                  ),
                )).toList(),
                onChanged: (v) => setDialogState(() => selectedProduct = v),
              ),
              
              const SizedBox(height: 16),
              
              // Estimated Time Info Box
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: AppColors.info.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: AppColors.info.withOpacity(0.3)),
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Row(
                      children: [
                        Icon(Icons.schedule, color: AppColors.info, size: 20),
                        const SizedBox(width: 8),
                        Text(
                          'Tahmini Süre',
                          style: TextStyle(fontWeight: FontWeight.bold, color: AppColors.info),
                        ),
                      ],
                    ),
                    Text(
                      productionDuration != null ? '$productionDuration Dakika' : '-',
                      style: TextStyle(fontWeight: FontWeight.bold, color: AppColors.info),
                    ),
                  ],
                ),
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context, false),
              child: const Text('İptal'),
            ),
            ElevatedButton.icon(
              onPressed: selectedProduct != null ? () => Navigator.pop(context, true) : null,
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.warning,
                foregroundColor: Colors.white,
              ),
              icon: const Icon(Icons.play_arrow),
              label: const Text('Onayla ve Başlat'),
            ),
          ],
        ),
      ),
    );

    if (confirmed != true || selectedProduct == null) return;

    setState(() => _isLoading = true);
    try {
      final api = ApiClient();
      await api.startProduction(building.id, selectedProduct!);
      ref.invalidate(buildingDetailProvider(widget.buildingId));
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Üretim başladı!'), backgroundColor: Colors.green),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(ApiClient.getErrorMessage(e)), backgroundColor: Colors.red),
        );
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  Future<void> _handleEditPrice(Building building, dynamic item) async {
    final itemId = item.id?.toString() ?? '';
    final itemName = item.name ?? 'Ürün';
    double newPrice = (item.price ?? 0).toDouble();
    
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setDialogState) => AlertDialog(
          title: Text('Fiyat Düzenle: $itemName'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Mevcut Stok: ${item.quantity} adet'),
              const SizedBox(height: 16),
              Text('Birim Fiyat', style: TextStyle(fontWeight: FontWeight.w600)),
              const SizedBox(height: 8),
              TextField(
                keyboardType: TextInputType.number,
                decoration: InputDecoration(
                  prefixIcon: Padding(
                    padding: EdgeInsets.all(12),
                    child: CurrencyIcon(size: 20),
                  ),
                  border: OutlineInputBorder(),
                  hintText: 'Fiyat girin',
                ),
                controller: TextEditingController(text: newPrice.toStringAsFixed(0)),
                onChanged: (v) => newPrice = double.tryParse(v) ?? newPrice,
              ),
              const SizedBox(height: 12),
              Text(
                'Bu fiyat satış sırasında kullanılacaktır.',
                style: TextStyle(color: AppColors.slate500, fontSize: 12),
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context, false),
              child: Text('İptal'),
            ),
            ElevatedButton.icon(
              onPressed: () => Navigator.pop(context, true),
              icon: Icon(Icons.save),
              label: Text('Kaydet'),
              style: ElevatedButton.styleFrom(backgroundColor: AppColors.primary),
            ),
          ],
        ),
      ),
    );
    
    if (confirmed == true && itemId.isNotEmpty) {
      setState(() => _isLoading = true);
      try {
        final api = ApiClient();
        await api.updateItemPrice(itemId, newPrice);
        ref.invalidate(buildingDetailProvider(widget.buildingId));
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('$itemName fiyatı güncellendi!'), backgroundColor: Colors.green),
          );
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text(ApiClient.getErrorMessage(e)), backgroundColor: Colors.red),
          );
        }
      } finally {
        if (mounted) setState(() => _isLoading = false);
      }
    }
  }

  Future<void> _handleCollectProduction(Building building) async {
    setState(() => _isLoading = true);
    try {
      final api = ApiClient();
      await api.collectProduction(building.id);
      ref.invalidate(buildingDetailProvider(widget.buildingId));
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Ürünler toplandı!'), backgroundColor: Colors.green),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(ApiClient.getErrorMessage(e)), backgroundColor: Colors.red),
        );
      }
      if (mounted) setState(() => _isLoading = false);
    }
  }

  Future<void> _handleTransferToInventory(Building building, BuildingItem item) async {
    int quantity = 1;
    
    await showDialog(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setDialogState) => AlertDialog(
          title: const Text('Envantere Aktar'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                '${item.name} ürününü genel envanterinize aktarmak istiyor musunuz?',
                style: TextStyle(color: AppColors.slate600),
              ),
              const SizedBox(height: 16),
              Text('Miktar', style: TextStyle(fontWeight: FontWeight.w500)),
              const SizedBox(height: 8),
              Row(
                children: [
                  IconButton(
                    icon: const Icon(Icons.remove_circle_outline),
                    onPressed: quantity > 1 ? () => setDialogState(() => quantity--) : null,
                  ),
                  Expanded(
                    child: Container(
                      padding: const EdgeInsets.symmetric(vertical: 12),
                      decoration: BoxDecoration(
                        border: Border.all(color: AppColors.slate300),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Text(
                        '$quantity',
                        textAlign: TextAlign.center,
                        style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                      ),
                    ),
                  ),
                  IconButton(
                    icon: const Icon(Icons.add_circle_outline),
                    onPressed: quantity < item.quantity ? () => setDialogState(() => quantity++) : null,
                  ),
                ],
              ),
              const SizedBox(height: 8),
              Text(
                'Maksimum: ${item.quantity}',
                style: TextStyle(fontSize: 12, color: AppColors.slate500),
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('İptal'),
            ),
            ElevatedButton(
              onPressed: () async {
                Navigator.pop(context);
                await _performTransfer(building, item.id, quantity);
              },
              child: const Text('Aktar'),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _performTransfer(Building building, String itemId, int quantity) async {
    setState(() => _isLoading = true);
    try {
      final api = ApiClient();
      await api.withdrawFromBuilding(building.id, productId, quantity);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Ürünler envantere aktarıldı!'), backgroundColor: Colors.green),
        );
        ref.invalidate(buildingDetailProvider(widget.buildingId));
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(ApiClient.getErrorMessage(e)), backgroundColor: Colors.red),
        );
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final buildingAsync = ref.watch(buildingDetailProvider(widget.buildingId));
    final pageTitle = Translations.getDetailPageTitle(widget.buildingType);

    return Scaffold(
      appBar: AppBar(
        title: Text(pageTitle),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => context.pop(),
        ),
      ),
      body: buildingAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stack) => Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(Icons.error_outline, size: 64, color: AppColors.error),
              const SizedBox(height: 16),
              Text('Hata: $error'),
              TextButton(
                onPressed: () => ref.invalidate(buildingDetailProvider(widget.buildingId)),
                child: const Text('Tekrar Dene'),
              ),
            ],
          ),
        ),
        data: (building) {
          if (building == null) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    BuildingIcons.getEmoji('SHOP'),
                    style: const TextStyle(fontSize: 48),
                  ),
                  const SizedBox(height: 16),
                  const Text('İşletme bulunamadı'),
                  TextButton(
                    onPressed: () => context.pop(),
                    child: const Text('Geri Dön'),
                  ),
                ],
              ),
            );
          }

          // Start timer if there's an active operation
          if (building.isSelling && building.salesEndsAt != null) {
            WidgetsBinding.instance.addPostFrameCallback((_) {
              _startTimer(building.salesEndsAt!);
            });
          } else if (building.isProducing && building.productionEndsAt != null) {
            WidgetsBinding.instance.addPostFrameCallback((_) {
              _startTimer(building.productionEndsAt!);
            });
          }

          return RefreshIndicator(
            onRefresh: () async {
              ref.invalidate(buildingDetailProvider(widget.buildingId));
            },
            child: SingleChildScrollView(
              physics: const AlwaysScrollableScrollPhysics(),
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Header Card
                  _buildHeaderCard(building),
                  const SizedBox(height: 16),
                  
                  // Stats Row
                  _buildStatsRow(building),
                  const SizedBox(height: 16),
                  
                  // Action Panel
                  _buildActionPanel(building),
                  const SizedBox(height: 16),
                  
                  // Inventory Section
                  _buildInventorySection(building),
                ],
              ),
            ),
          );
        },
      ),
    );
  }

  Widget _buildHeaderCard(Building building) {
    final typeEmoji = BuildingIcons.getEmoji(widget.buildingType);
    final subTypeTr = Translations.getBuildingSubTypeTr(building.subType);
    final tierLevel = Translations.getTierLevel(building.tier);
    final tierColor = _getTierColor(building.tier);

    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            AppColors.slate800,
            AppColors.slate900,
          ],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(
          color: tierColor.withOpacity(0.3),
          width: 1,
        ),
        boxShadow: [
          BoxShadow(
            color: tierColor.withOpacity(0.2),
            blurRadius: 20,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: Column(
        children: [
          Row(
            children: [
              // Emoji with glowing background
              Container(
                width: 72,
                height: 72,
                decoration: BoxDecoration(
                  gradient: RadialGradient(
                    colors: [
                      tierColor.withOpacity(0.3),
                      tierColor.withOpacity(0.1),
                      Colors.transparent,
                    ],
                    stops: const [0.0, 0.5, 1.0],
                  ),
                  borderRadius: BorderRadius.circular(18),
                  border: Border.all(
                    color: tierColor.withOpacity(0.4),
                    width: 2,
                  ),
                ),
                child: Center(
                  child: Text(typeEmoji, style: const TextStyle(fontSize: 36)),
                ),
              ),
              const SizedBox(width: 16),
              
              // Name and details
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      building.name,
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 22,
                        fontWeight: FontWeight.bold,
                        letterSpacing: -0.5,
                      ),
                    ),
                    const SizedBox(height: 8),
                    Row(
                      children: [
                        // Type badge
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                          decoration: BoxDecoration(
                            color: AppColors.slate700,
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: Text(
                            subTypeTr,
                            style: TextStyle(
                              color: AppColors.slate300,
                              fontSize: 12,
                              fontWeight: FontWeight.w500,
                            ),
                          ),
                        ),
                        const SizedBox(width: 8),
                        // Level badge
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                          decoration: BoxDecoration(
                            gradient: LinearGradient(
                              colors: [tierColor, tierColor.withOpacity(0.7)],
                            ),
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: Row(
                            children: [
                              const Icon(Icons.star, size: 12, color: Colors.white),
                              const SizedBox(width: 4),
                              Text(
                                'Seviye $tierLevel',
                                style: const TextStyle(
                                  color: Colors.white,
                                  fontSize: 12,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Color _getTierColor(String tier) {
    switch (tier.toUpperCase()) {
      case 'SMALL':
        return AppColors.success;
      case 'MEDIUM':
        return AppColors.info;
      case 'LARGE':
        return const Color(0xFFB45309); // Amber/Gold
      default:
        return AppColors.primary;
    }
  }

  Widget _buildStatsRow(Building building) {
    final tierTr = Translations.getTierTr(building.tier);
    final currentStock = building.items.fold<int>(0, (sum, item) => sum + item.quantity);
    final maxStock = building.maxStock ?? 100;
    final stockPercentage = maxStock > 0 ? (currentStock / maxStock) : 0.0;

    return Row(
      children: [
        // Tier/Capacity Card
        Expanded(
          child: _buildStatCard(
            emoji: BuildingIcons.getEmoji(building.type),
            label: 'Kapasite',
            value: tierTr,
            subValue: 'Max: $maxStock Birim',
            color: AppColors.warning,
          ),
        ),
        const SizedBox(width: 12),
        
        // Stock Card
        Expanded(
          child: _buildStatCard(
            emoji: '📦',
            label: 'Doluluk',
            value: '$currentStock/$maxStock',
            subValue: '${(stockPercentage * 100).toStringAsFixed(0)}% dolu',
            color: AppColors.info,
            showProgress: true,
            progress: stockPercentage,
          ),
        ),
        const SizedBox(width: 12),
        
        // Revenue or Product Type Card (based on building type)
        Expanded(
          child: isShop
              ? _buildRevenueCard(building)
              : _buildProductTypeCard(building),
        ),
      ],
    );
  }

  Widget _buildRevenueCard(Building building) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.slate200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(Icons.monetization_on, size: 16, color: AppColors.success),
              const SizedBox(width: 4),
              Text('Son Ciro', style: TextStyle(color: AppColors.slate500, fontSize: 10)),
            ],
          ),
          const SizedBox(height: 8),
          Row(
            children: [
              const CurrencyIcon(size: 14),
              const SizedBox(width: 4),
              Expanded(
                child: Text(
                  '${building.lastRevenue ?? 0}',
                  style: TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 14,
                    color: AppColors.success,
                  ),
                  overflow: TextOverflow.ellipsis,
                ),
              ),
            ],
          ),
          const SizedBox(height: 4),
          Text(
            'Son Satış',
            style: TextStyle(color: AppColors.slate400, fontSize: 10),
          ),
        ],
      ),
    );
  }

  Widget _buildProductTypeCard(Building building) {
    final productCount = building.items.map((i) => i.name).toSet().length;
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.slate200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(Icons.category, size: 16, color: AppColors.primary),
              const SizedBox(width: 4),
              Text('Ürün Tipi', style: TextStyle(color: AppColors.slate500, fontSize: 10)),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            '$productCount Adet',
            style: TextStyle(
              fontWeight: FontWeight.bold,
              fontSize: 14,
              color: AppColors.primary,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            'Üretilen Çeşit',
            style: TextStyle(color: AppColors.slate400, fontSize: 10),
          ),
        ],
      ),
    );
  }

  Widget _buildStatCard({
    required String emoji,
    required String label,
    required String value,
    required String subValue,
    required Color color,
    bool showProgress = false,
    double progress = 0,
  }) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.slate200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Text(emoji, style: const TextStyle(fontSize: 16)),
              const SizedBox(width: 4),
              Expanded(
                child: Text(
                  label, 
                  style: TextStyle(color: AppColors.slate500, fontSize: 10),
                  overflow: TextOverflow.ellipsis,
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            value,
            style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
          ),
          if (showProgress) ...[
            const SizedBox(height: 4),
            ClipRRect(
              borderRadius: BorderRadius.circular(4),
              child: LinearProgressIndicator(
                value: progress.clamp(0.0, 1.0),
                backgroundColor: AppColors.slate200,
                valueColor: AlwaysStoppedAnimation(color),
                minHeight: 4,
              ),
            ),
          ] else ...[
            const SizedBox(height: 4),
            Text(
              subValue,
              style: TextStyle(color: AppColors.slate400, fontSize: 10),
              overflow: TextOverflow.ellipsis,
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildActionPanel(Building building) {
    final isActive = building.isSelling || building.isProducing;
    final canComplete = _remainingTime == Duration.zero && isActive;

    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.slate200),
      ),
      child: Column(
        children: [
          // Status Header
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                isShop ? 'Satış İşlemleri' : 'Üretim Hattı',
                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                decoration: BoxDecoration(
                  color: isActive 
                      ? AppColors.warning.withOpacity(0.1) 
                      : AppColors.slate100,
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(
                    color: isActive ? AppColors.warning : AppColors.slate300,
                  ),
                ),
                child: Row(
                  children: [
                    Container(
                      width: 8,
                      height: 8,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color: isActive ? AppColors.warning : AppColors.slate400,
                      ),
                    ),
                    const SizedBox(width: 6),
                    Text(
                      isActive ? (isShop ? 'SATIŞ AKTİF' : 'HAT AKTİF') : 'DURDURULDU',
                      style: TextStyle(
                        fontSize: 10,
                        fontWeight: FontWeight.bold,
                        color: isActive ? AppColors.warning : AppColors.slate500,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 20),

          // Active State
          if (isActive) ...[
            // Timer Circle
            Container(
              width: 120,
              height: 120,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                border: Border.all(color: AppColors.warning, width: 4),
              ),
              child: Center(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Text(
                      'Kalan',
                      style: TextStyle(color: AppColors.slate500, fontSize: 12),
                    ),
                    Text(
                      _formatDuration(_remainingTime),
                      style: const TextStyle(
                        fontSize: 24,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 20),

            // Complete Button
            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: canComplete && !_isLoading
                    ? () => isShop ? _handleCompleteSale(building) : _handleCollectProduction(building)
                    : null,
                style: ElevatedButton.styleFrom(
                  backgroundColor: canComplete ? AppColors.success : AppColors.slate300,
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(vertical: 16),
                ),
                icon: Icon(canComplete ? Icons.check : Icons.timer),
                label: Text(
                  canComplete 
                      ? (isShop ? 'Satışı Tamamla' : 'Ürünleri Topla')
                      : 'Devam Ediyor...',
                  style: const TextStyle(fontWeight: FontWeight.bold),
                ),
              ),
            ),
          ] else ...[
            // Idle State
            Container(
              padding: const EdgeInsets.all(24),
              child: Column(
                children: [
                  Text(
                    BuildingIcons.getEmoji(building.type),
                    style: const TextStyle(fontSize: 48),
                  ),
                  const SizedBox(height: 12),
                  Text(
                    isShop ? 'Dükkan Beklemede' : 'Tesis Beklemede',
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                      color: AppColors.slate600,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    isShop 
                        ? 'Stoklar satışa hazır. Satışı başlatın.'
                        : 'Yeni üretim emri vermek için butona tıklayın.',
                    style: TextStyle(color: AppColors.slate400, fontSize: 12),
                    textAlign: TextAlign.center,
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),

            // Start Button
            SizedBox(
              width: double.infinity,
              child: Builder(
                builder: (context) {
                  // For shops, check if there are items with quantity > 0
                  final hasInventory = !isShop || building.items.any((item) => item.quantity > 0);
                  final canStart = !_isLoading && hasInventory;
                  
                  return Column(
                    children: [
                      ElevatedButton.icon(
                        onPressed: canStart
                            ? () => isShop ? _handleStartSales(building) : _handleStartProduction(building)
                            : null,
                        style: ElevatedButton.styleFrom(
                          backgroundColor: canStart ? AppColors.warning : AppColors.slate400,
                          foregroundColor: Colors.white,
                          padding: const EdgeInsets.symmetric(vertical: 16),
                          minimumSize: const Size(double.infinity, 56),
                        ),
                        icon: const Icon(Icons.play_arrow),
                        label: Text(
                          isShop ? 'Satışı Başlat' : 'Üretimi Başlat',
                          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                        ),
                      ),
                      if (isShop && !hasInventory)
                        Padding(
                          padding: const EdgeInsets.only(top: 8),
                          child: Text(
                            'Envanter boş, önce ürün eklemelisiniz',
                            style: TextStyle(color: AppColors.error, fontSize: 12),
                          ),
                        ),
                    ],
                  );
                },
              ),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildInventorySection(Building building) {
    // Filter out items with 0 quantity (production in progress)
    final visibleItems = building.items.where((item) => item.quantity > 0).toList();
    
    return Container(
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.slate200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header
          Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text(
                  'Depo Envanteri',
                  style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                ),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                  decoration: BoxDecoration(
                    color: AppColors.slate100,
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Text(
                    '${visibleItems.length} Kalem',
                    style: TextStyle(
                      color: AppColors.slate600,
                      fontSize: 12,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ],
            ),
          ),
          const Divider(height: 1),

          // Items List
          if (visibleItems.isEmpty)
            Padding(
              padding: const EdgeInsets.all(32),
              child: Center(
                child: Column(
                  children: [
                    Icon(Icons.inventory_2, size: 48, color: AppColors.slate300),
                    const SizedBox(height: 12),
                    Text(
                      'Depo boş',
                      style: TextStyle(color: AppColors.slate400),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      isShop ? 'Envanterden ürün transfer edin' : 'Üretim yaparak stok oluşturun',
                      style: TextStyle(color: AppColors.slate400, fontSize: 12),
                    ),
                  ],
                ),
              ),
            )
          else
            ListView.separated(
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              itemCount: visibleItems.length,
              separatorBuilder: (_, __) => const Divider(height: 1),
              itemBuilder: (context, index) {
                final item = visibleItems[index];
                return ListTile(
                  onTap: isShop 
                      ? () => _handleEditPrice(building, item) 
                      : () => _handleTransferToInventory(building, item),
                  leading: Container(
                    width: 40,
                    height: 40,
                    decoration: BoxDecoration(
                      color: AppColors.primary.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Center(
                      child: ProductEmoji(productName: item.name, size: 24),
                    ),
                  ),
                  title: Text(item.name, style: const TextStyle(fontWeight: FontWeight.w600)),
                  subtitle: Row(
                    children: [
                      _buildStarRating(item.qualityScore.toDouble()),
                      const SizedBox(width: 8),
                      Text('${item.quantity} adet', style: TextStyle(color: AppColors.slate500, fontSize: 12)),
                    ],
                  ),
                  trailing: isShop
                      ? Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            const CurrencyIcon(size: 14),
                            const SizedBox(width: 4),
                            Text(
                              '${item.price ?? 0}',
                              style: TextStyle(
                                fontWeight: FontWeight.bold,
                                color: AppColors.success,
                              ),
                            ),
                            const SizedBox(width: 8),
                            Icon(Icons.edit, size: 16, color: AppColors.slate400),
                          ],
                        )
                      : Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Icon(Icons.move_to_inbox, size: 18, color: AppColors.primary),
                            const SizedBox(width: 4),
                            Text(
                              'Aktar',
                              style: TextStyle(
                                fontSize: 12,
                                color: AppColors.primary,
                                fontWeight: FontWeight.w500,
                              ),
                            ),
                          ],
                        ),
                );
              },
            ),
        ],
      ),
    );
  }

  Widget _buildStarRating(double score) {
    final stars = (score / 20).clamp(0.0, 5.0);
    final fullStars = stars.floor();
    final hasHalfStar = (stars - fullStars) >= 0.5;

    return Row(
      mainAxisSize: MainAxisSize.min,
      children: List.generate(5, (index) {
        if (index < fullStars) {
          return Icon(Icons.star, size: 12, color: AppColors.warning);
        } else if (index == fullStars && hasHalfStar) {
          return Icon(Icons.star_half, size: 12, color: AppColors.warning);
        } else {
          return Icon(Icons.star_border, size: 12, color: AppColors.slate300);
        }
      }),
    );
  }
}
