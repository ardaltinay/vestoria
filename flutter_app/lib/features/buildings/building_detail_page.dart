import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'dart:async';
import '../../core/theme/app_theme.dart';
import '../../core/api/api_client.dart';
import '../../data/models/building.dart';
import '../../core/widgets/currency_icon.dart';
import '../../core/widgets/product_emoji.dart';
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
      ref.invalidate(buildingDetailProvider(widget.buildingId));
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
          SnackBar(content: Text('Hata: $e'), backgroundColor: Colors.red),
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
          SnackBar(content: Text('Hata: $e'), backgroundColor: Colors.red),
        );
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  Future<void> _handleStartProduction(Building building) async {
    // Show product selection dialog
    final products = building.items.map((i) => i.name).toSet().toList();
    if (products.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Üretilecek ürün bulunamadı'), backgroundColor: Colors.orange),
      );
      return;
    }
    
    final selectedProduct = await showDialog<String>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Üretilecek Ürün Seçin'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: products.map((p) => ListTile(
            leading: ProductEmoji(productName: p, size: 24),
            title: Text(p),
            onTap: () => Navigator.pop(context, p),
          )).toList(),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('İptal'),
          ),
        ],
      ),
    );

    if (selectedProduct == null) return;

    setState(() => _isLoading = true);
    try {
      final api = ApiClient();
      await api.startProduction(building.id, selectedProduct);
      ref.invalidate(buildingDetailProvider(widget.buildingId));
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Üretim başladı!'), backgroundColor: Colors.green),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Hata: $e'), backgroundColor: Colors.red),
        );
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
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
          SnackBar(content: Text('Hata: $e'), backgroundColor: Colors.red),
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
                  Icon(Icons.store_mall_directory, size: 64, color: AppColors.slate400),
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
    final typeEmoji = _getBuildingEmoji(widget.buildingType);
    final subTypeTr = Translations.getBuildingSubTypeTr(building.subType);
    final tierLevel = Translations.getTierLevel(building.tier);

    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [AppColors.primary, AppColors.primaryDark],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Row(
        children: [
          // Icon with level badge
          Stack(
            children: [
              Container(
                width: 64,
                height: 64,
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.2),
                  borderRadius: BorderRadius.circular(16),
                ),
                child: Center(
                  child: Text(typeEmoji, style: const TextStyle(fontSize: 32)),
                ),
              ),
              Positioned(
                bottom: -4,
                right: -4,
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                  decoration: BoxDecoration(
                    color: AppColors.slate900,
                    borderRadius: BorderRadius.circular(8),
                    border: Border.all(color: Colors.white, width: 2),
                  ),
                  child: Text(
                    'LVL $tierLevel',
                    style: const TextStyle(color: Colors.white, fontSize: 10, fontWeight: FontWeight.bold),
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(width: 16),
          
          // Name and Type
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  building.name,
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 20,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 4),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                  decoration: BoxDecoration(
                    color: Colors.white.withOpacity(0.2),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Text(
                    subTypeTr,
                    style: const TextStyle(color: Colors.white, fontSize: 12),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
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
            icon: Icons.business,
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
            icon: Icons.inventory_2,
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
    required IconData icon,
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
              Icon(icon, size: 16, color: color),
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
                  Icon(
                    isShop ? Icons.store : Icons.factory,
                    size: 48,
                    color: AppColors.slate300,
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
              child: ElevatedButton.icon(
                onPressed: _isLoading
                    ? null
                    : () => isShop ? _handleStartSales(building) : _handleStartProduction(building),
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.warning,
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(vertical: 16),
                ),
                icon: const Icon(Icons.play_arrow),
                label: Text(
                  isShop ? 'Satışı Başlat' : 'Üretimi Başlat',
                  style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                ),
              ),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildInventorySection(Building building) {
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
                    '${building.items.length} Kalem',
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
          if (building.items.isEmpty)
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
              itemCount: building.items.length,
              separatorBuilder: (_, __) => const Divider(height: 1),
              itemBuilder: (context, index) {
                final item = building.items[index];
                return ListTile(
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
                          ],
                        )
                      : null,
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

  String _getBuildingEmoji(String type) {
    switch (type.toUpperCase()) {
      case 'SHOP': return '🏪';
      case 'FARM': return '🌾';
      case 'FACTORY': return '🏭';
      case 'MINE': return '⛏️';
      case 'GARDEN': return '🌸';
      default: return '🏢';
    }
  }
}
