import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../core/theme/app_theme.dart';
import '../../core/api/api_client.dart';
import '../../core/widgets/currency_icon.dart';
import '../../core/widgets/building_icons.dart';
import 'package:intl/intl.dart';

// Building stats provider
final buildingStatsProvider = FutureProvider<Map<String, dynamic>>((ref) async {
  final api = ApiClient();
  
  try {
    final buildingsResponse = await api.getAllBuildings();
    final List buildings = buildingsResponse.data ?? [];
    
    int shopCount = 0, farmCount = 0, factoryCount = 0, mineCount = 0, gardenCount = 0;
    num totalRevenue = 0;
    num totalStock = 0;
    
    for (final building in buildings) {
      final type = (building['type'] ?? '').toString().toUpperCase();
      final lastRevenue = (building['lastRevenue'] ?? 0) as num;
      final currentStock = (building['currentStock'] ?? 0) as num;
      
      totalRevenue += lastRevenue;
      totalStock += currentStock;
      
      switch (type) {
        case 'SHOP': shopCount++; break;
        case 'FARM': farmCount++; break;
        case 'FACTORY': factoryCount++; break;
        case 'MINE': mineCount++; break;
        case 'GARDEN': gardenCount++; break;
      }
    }
    
    final inventoryResponse = await api.getInventory();
    final List inventory = inventoryResponse.data ?? [];
    int inventoryItems = 0;
    for (final item in inventory) {
      inventoryItems += (item['quantity'] ?? 0) as int;
    }
    
    return {
      'shopCount': shopCount,
      'farmCount': farmCount,
      'factoryCount': factoryCount,
      'mineCount': mineCount,
      'gardenCount': gardenCount,
      'totalBuildings': buildings.length,
      'totalRevenue': totalRevenue,
      'totalStock': totalStock,
      'inventoryItems': inventoryItems,
      'buildings': buildings,
    };
  } catch (e) {
    print('Reports stats error: $e');
    return {
      'shopCount': 0,
      'farmCount': 0,
      'factoryCount': 0,
      'mineCount': 0,
      'gardenCount': 0,
      'totalBuildings': 0,
      'totalRevenue': 0,
      'totalStock': 0,
      'inventoryItems': 0,
      'buildings': [],
    };
  }
});

class ReportsPage extends ConsumerWidget {
  const ReportsPage({super.key});

  String formatCurrency(num amount) {
    final formatter = NumberFormat.decimalPattern('tr_TR');
    return formatter.format(amount);
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final statsAsync = ref.watch(buildingStatsProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Raporlar'),
      ),
      body: RefreshIndicator(
        onRefresh: () async {
          ref.invalidate(buildingStatsProvider);
        },
        child: statsAsync.when(
          loading: () => const Center(child: CircularProgressIndicator()),
          error: (error, stack) => Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(Icons.error_outline, size: 64, color: AppColors.error),
                const SizedBox(height: 16),
                Text('Hata: $error'),
                TextButton(
                  onPressed: () => ref.invalidate(buildingStatsProvider),
                  child: const Text('Tekrar Dene'),
                ),
              ],
            ),
          ),
          data: (stats) => SingleChildScrollView(
            physics: const AlwaysScrollableScrollPhysics(),
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Overview Section
                Text(
                  'Genel Bakış',
                  style: Theme.of(context).textTheme.headlineSmall,
                ),
                const SizedBox(height: 12),
                _buildOverviewCards(context, stats),
                const SizedBox(height: 24),
                
                // Building Distribution
                Text(
                  'İşletme Dağılımı',
                  style: Theme.of(context).textTheme.headlineSmall,
                ),
                const SizedBox(height: 12),
                _buildDistributionCard(context, stats),
                const SizedBox(height: 24),
                
                // Building List
                Text(
                  'İşletme Listesi',
                  style: Theme.of(context).textTheme.headlineSmall,
                ),
                const SizedBox(height: 12),
                _buildBuildingList(context, stats['buildings'] ?? []),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildOverviewCards(BuildContext context, Map<String, dynamic> stats) {
    return GridView.count(
      crossAxisCount: 2,
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      crossAxisSpacing: 12,
      mainAxisSpacing: 12,
      childAspectRatio: 1.4,
      children: [
        _buildStatCard(
          context,
          emoji: '🏢',
          title: 'Toplam İşletme',
          value: '${stats['totalBuildings'] ?? 0}',
          color: AppColors.primary,
        ),
        _buildStatCard(
          context,
          emoji: '📦',
          title: 'Toplam Stok',
          value: '${stats['totalStock'] ?? 0}',
          color: AppColors.info,
        ),
        _buildStatCard(
          context,
          emoji: '🎒',
          title: 'Envanter Ürünleri',
          value: '${stats['inventoryItems'] ?? 0}',
          color: AppColors.warning,
        ),
        Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: AppColors.surface,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: AppColors.slate200),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Row(
                children: [
                  const Text('💰', style: TextStyle(fontSize: 20)),
                  const SizedBox(width: 8),
                  Text(
                    'Toplam Ciro',
                    style: TextStyle(
                      color: AppColors.slate500,
                      fontSize: 12,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              Row(
                children: [
                  const CurrencyIcon(size: 18),
                  const SizedBox(width: 6),
                  Expanded(
                    child: Text(
                      formatCurrency(stats['totalRevenue'] ?? 0),
                      style: TextStyle(
                        fontWeight: FontWeight.bold,
                        fontSize: 18,
                        color: AppColors.success,
                      ),
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildStatCard(
    BuildContext context, {
    required String emoji,
    required String title,
    required String value,
    required Color color,
  }) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.slate200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Row(
            children: [
              Text(emoji, style: const TextStyle(fontSize: 20)),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  title,
                  style: TextStyle(color: AppColors.slate500, fontSize: 12),
                  overflow: TextOverflow.ellipsis,
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            value,
            style: TextStyle(
              fontWeight: FontWeight.bold,
              fontSize: 24,
              color: color,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDistributionCard(BuildContext context, Map<String, dynamic> stats) {
    final items = [
      (BuildingIcons.shopEmoji, 'Dükkanlar', stats['shopCount'] ?? 0),
      (BuildingIcons.gardenEmoji, 'Bahçeler', stats['gardenCount'] ?? 0),
      (BuildingIcons.farmEmoji, 'Çiftlikler', stats['farmCount'] ?? 0),
      (BuildingIcons.factoryEmoji, 'Fabrikalar', stats['factoryCount'] ?? 0),
      (BuildingIcons.mineEmoji, 'Madenler', stats['mineCount'] ?? 0),
    ];

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.slate200),
      ),
      child: Column(
        children: items.map((item) {
          final total = stats['totalBuildings'] ?? 1;
          final percentage = total > 0 ? (item.$3 / total) : 0.0;
          
          return Padding(
            padding: const EdgeInsets.symmetric(vertical: 6),
            child: Row(
              children: [
                Text(item.$1, style: const TextStyle(fontSize: 20)),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(item.$2, style: const TextStyle(fontWeight: FontWeight.w500)),
                          Text('${item.$3}', style: TextStyle(fontWeight: FontWeight.bold, color: AppColors.primary)),
                        ],
                      ),
                      const SizedBox(height: 4),
                      ClipRRect(
                        borderRadius: BorderRadius.circular(4),
                        child: LinearProgressIndicator(
                          value: percentage,
                          backgroundColor: AppColors.slate100,
                          valueColor: AlwaysStoppedAnimation(AppColors.primary),
                          minHeight: 6,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          );
        }).toList(),
      ),
    );
  }

  Widget _buildBuildingList(BuildContext context, List buildings) {
    if (buildings.isEmpty) {
      return Container(
        padding: const EdgeInsets.all(32),
        decoration: BoxDecoration(
          color: AppColors.surface,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: AppColors.slate200),
        ),
        child: Center(
          child: Column(
            children: [
              Text(BuildingIcons.shopEmoji, style: const TextStyle(fontSize: 48)),
              const SizedBox(height: 12),
              Text('Henüz işletmeniz yok', style: TextStyle(color: AppColors.slate400)),
            ],
          ),
        ),
      );
    }

    return Container(
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.slate200),
      ),
      child: ListView.separated(
        shrinkWrap: true,
        physics: const NeverScrollableScrollPhysics(),
        itemCount: buildings.length,
        separatorBuilder: (_, __) => const Divider(height: 1),
        itemBuilder: (context, index) {
          final building = buildings[index];
          final type = (building['type'] ?? '').toString().toUpperCase();
          final emoji = BuildingIcons.getEmoji(type);
          final name = building['name'] ?? 'İşletme';
          final tier = building['tier'] ?? 'SMALL';
          final currentStock = building['currentStock'] ?? 0;
          final maxStock = building['maxStock'] ?? 100;
          
          return ListTile(
            onTap: () {
              final id = building['id'].toString();
              String route;
              switch (type) {
                case 'SHOP': route = '/shops/$id'; break;
                case 'FARM': route = '/farms/$id'; break;
                case 'FACTORY': route = '/factories/$id'; break;
                case 'MINE': route = '/mines/$id'; break;
                case 'GARDEN': route = '/gardens/$id'; break;
                default: route = '/shops/$id';
              }
              GoRouter.of(context).push(route);
            },
            leading: Container(
              width: 40,
              height: 40,
              decoration: BoxDecoration(
                color: AppColors.primary.withOpacity(0.1),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Center(child: Text(emoji, style: const TextStyle(fontSize: 20))),
            ),
            title: Text(name, style: const TextStyle(fontWeight: FontWeight.w600)),
            subtitle: Text(
              '${_getTierLabel(tier)} • $currentStock/$maxStock stok',
              style: TextStyle(color: AppColors.slate500, fontSize: 12),
            ),
            trailing: Icon(Icons.chevron_right, color: AppColors.slate400),
          );
        },
      ),
    );
  }

  String _getTierLabel(String tier) {
    switch (tier.toUpperCase()) {
      case 'SMALL': return 'Küçük';
      case 'MEDIUM': return 'Orta';
      case 'LARGE': return 'Büyük';
      default: return tier;
    }
  }
}
