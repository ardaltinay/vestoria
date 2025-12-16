import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../core/theme/app_theme.dart';
import '../../core/providers/auth_provider.dart';
import '../../core/api/api_client.dart';
import '../../core/widgets/currency_icon.dart';
import '../../core/widgets/building_icons.dart';
import 'package:intl/intl.dart';

// Dashboard stats computed from buildings and inventory
final dashboardStatsProvider = FutureProvider<Map<String, int>>((ref) async {
  final api = ApiClient();
  
  try {
    // Get all buildings
    final buildingsResponse = await api.getAllBuildings();
    final List buildings = buildingsResponse.data ?? [];
    
    // Count by type
    int shopCount = 0;
    int farmCount = 0;
    int factoryCount = 0;
    int mineCount = 0;
    int gardenCount = 0;
    
    for (final building in buildings) {
      final type = (building['type'] ?? '').toString().toUpperCase();
      switch (type) {
        case 'SHOP':
          shopCount++;
          break;
        case 'FARM':
          farmCount++;
          break;
        case 'FACTORY':
          factoryCount++;
          break;
        case 'MINE':
          mineCount++;
          break;
        case 'GARDEN':
          gardenCount++;
          break;
      }
    }
    
    // Get inventory count
    final inventoryResponse = await api.getInventory();
    final List inventory = inventoryResponse.data ?? [];
    int inventoryCount = 0;
    for (final item in inventory) {
      inventoryCount += (item['quantity'] ?? 0) as int;
    }
    
    return {
      'shopCount': shopCount,
      'farmCount': farmCount,
      'factoryCount': factoryCount,
      'mineCount': mineCount,
      'gardenCount': gardenCount,
      'inventoryCount': inventoryCount,
      'totalBuildings': buildings.length,
    };
  } catch (e) {
    print('Dashboard stats error: $e');
    return {
      'shopCount': 0,
      'farmCount': 0,
      'factoryCount': 0,
      'mineCount': 0,
      'gardenCount': 0,
      'inventoryCount': 0,
      'totalBuildings': 0,
    };
  }
});

// Market trends provider
final marketTrendsProvider = FutureProvider<List<Map<String, dynamic>>>((ref) async {
  final api = ApiClient();
  try {
    final response = await api.getMarketTrends();
    final List data = response.data ?? [];
    return data.map<Map<String, dynamic>>((item) => Map<String, dynamic>.from(item)).toList();
  } catch (e) {
    print('Market trends error: $e');
    return [];
  }
});

// Production summary provider - fetches buildings with their production stats
final productionSummaryProvider = FutureProvider<List<Map<String, dynamic>>>((ref) async {
  final api = ApiClient();
  try {
    final response = await api.getAllBuildings();
    final List buildings = response.data ?? [];
    
    // Filter buildings that have production (farms, factories, gardens, mines)
    final productionBuildings = buildings
        .where((b) => ['FARM', 'FACTORY', 'GARDEN', 'MINE'].contains(b['type']))
        .map<Map<String, dynamic>>((b) => Map<String, dynamic>.from(b))
        .toList();
    
    return productionBuildings;
  } catch (e) {
    print('Production summary error: $e');
    return [];
  }
});

class DashboardPage extends ConsumerWidget {
  const DashboardPage({super.key});

  String formatCurrency(num amount) {
    final formatter = NumberFormat.currency(locale: 'tr_TR', symbol: '', decimalDigits: 0);
    return formatter.format(amount);
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final authState = ref.watch(authProvider);
    final user = authState.user;
    final statsAsync = ref.watch(dashboardStatsProvider);

    return RefreshIndicator(
      onRefresh: () async {
        // Only refresh dashboard stats, don't invalidate auth state
        ref.invalidate(dashboardStatsProvider);
      },
      child: SingleChildScrollView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Welcome Card
            // Welcome Banner
            Container(
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
                  color: AppColors.primary.withOpacity(0.3),
                  width: 1,
                ),
                boxShadow: [
                  BoxShadow(
                    color: AppColors.primary.withOpacity(0.1),
                    blurRadius: 20,
                    offset: const Offset(0, 8),
                  ),
                ],
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      // User avatar
                      Container(
                        width: 48,
                        height: 48,
                        decoration: BoxDecoration(
                          gradient: RadialGradient(
                            colors: [
                              AppColors.primary.withOpacity(0.4),
                              AppColors.primary.withOpacity(0.1),
                            ],
                          ),
                          borderRadius: BorderRadius.circular(12),
                          border: Border.all(
                            color: AppColors.primary.withOpacity(0.5),
                            width: 2,
                          ),
                        ),
                        child: const Center(
                          child: Text('👋', style: TextStyle(fontSize: 24)),
                        ),
                      ),
                      const SizedBox(width: 16),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              'Hoş geldiniz!',
                              style: TextStyle(
                                color: AppColors.slate400,
                                fontSize: 12,
                              ),
                            ),
                            const SizedBox(height: 2),
                            Text(
                              user?.username ?? 'Oyuncu',
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 20,
                                fontWeight: FontWeight.bold,
                                letterSpacing: -0.5,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  // Balance display
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                    decoration: BoxDecoration(
                      color: AppColors.slate700.withOpacity(0.5),
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(
                        color: AppColors.slate600,
                        width: 1,
                      ),
                    ),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          'Bakiye',
                          style: TextStyle(
                            color: AppColors.slate400,
                            fontSize: 14,
                          ),
                        ),
                        Row(
                          children: [
                            const CurrencyIcon(size: 20),
                            const SizedBox(width: 8),
                            Text(
                              formatCurrency(user?.balance ?? 0),
                              style: TextStyle(
                                color: AppColors.success,
                                fontWeight: FontWeight.bold,
                                fontSize: 18,
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 24),

            // Stats Grid
            Text(
              'Genel Bakış',
              style: Theme.of(context).textTheme.headlineSmall,
            ),
            const SizedBox(height: 12),
            
            statsAsync.when(
              data: (stats) => _buildStatsGrid(context, stats),
              loading: () => _buildLoadingGrid(),
              error: (error, stack) => _buildStatsGrid(context, {}),
            ),
            
            const SizedBox(height: 24),
            
            // Market Trends Section
            Text(
              '📈 Pazar Trendleri',
              style: Theme.of(context).textTheme.headlineSmall,
            ),
            const SizedBox(height: 12),
            
            ref.watch(marketTrendsProvider).when(
              data: (trends) => _buildMarketTrends(context, trends),
              loading: () => _buildLoadingCard(),
              error: (e, s) => _buildEmptyCard('Trend verisi yüklenemedi'),
            ),
            
            const SizedBox(height: 24),
            
            // Production Summary Section
            Text(
              '🏭 Üretim Özeti',
              style: Theme.of(context).textTheme.headlineSmall,
            ),
            const SizedBox(height: 12),
            
            ref.watch(productionSummaryProvider).when(
              data: (buildings) => _buildProductionSummary(context, buildings),
              loading: () => _buildLoadingCard(),
              error: (e, s) => _buildEmptyCard('Üretim verisi yüklenemedi'),
            ),
            
            const SizedBox(height: 24),
            
            // Quick Actions
            Text(
              'Hızlı İşlemler',
              style: Theme.of(context).textTheme.headlineSmall,
            ),
            const SizedBox(height: 12),
            
            _buildQuickActions(context),
          ],
        ),
      ),
    );
  }
  
  Widget _buildMarketTrends(BuildContext context, List<Map<String, dynamic>> trends) {
    // Filter out trends with no valid data
    final validTrends = trends.where((trend) {
      final name = trend['itemName'];
      final hasName = name != null && name.toString().trim().isNotEmpty;
      final hasPrice = trend['avgPrice'] != null && (trend['avgPrice'] as num) > 0;
      return hasName || hasPrice;
    }).toList();
    
    if (validTrends.isEmpty) {
      return _buildEmptyCard('Henüz pazar trendi yok');
    }
    
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.slate50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.slate200),
      ),
      child: Column(
        children: validTrends.take(5).map((trend) {
          final rawName = trend['itemName'];
          final itemName = (rawName == null || rawName.toString().trim().isEmpty) ? '-' : rawName.toString();
          
          final rawPrice = trend['avgPrice'];
          final hasPrice = rawPrice != null && (rawPrice as num) > 0;
          final avgPrice = hasPrice ? (rawPrice as num).toDouble() : 0.0;
          
          final rawChange = trend['priceChange'];
          final hasChange = rawChange != null;
          final priceChange = hasChange ? (rawChange as num).toDouble() : 0.0;
          final isUp = priceChange >= 0;
          
          return Padding(
            padding: const EdgeInsets.symmetric(vertical: 8),
            child: Row(
              children: [
                Expanded(
                  child: Text(
                    itemName,
                    style: const TextStyle(fontWeight: FontWeight.w500),
                  ),
                ),
                Row(
                  children: [
                    if (hasPrice) ...[
                      const CurrencyIcon(size: 12),
                      const SizedBox(width: 4),
                      Text(
                        formatCurrency(avgPrice),
                        style: const TextStyle(fontWeight: FontWeight.bold),
                      ),
                    ] else
                      const Text('-', style: TextStyle(fontWeight: FontWeight.bold)),
                  ],
                ),
                const SizedBox(width: 12),
                hasChange
                  ? Container(
                      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                      decoration: BoxDecoration(
                        color: isUp ? AppColors.success.withOpacity(0.1) : AppColors.error.withOpacity(0.1),
                        borderRadius: BorderRadius.circular(4),
                      ),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Icon(
                            isUp ? Icons.arrow_upward : Icons.arrow_downward,
                            size: 12,
                            color: isUp ? AppColors.success : AppColors.error,
                          ),
                          Text(
                            '${priceChange.abs().toStringAsFixed(1)}%',
                            style: TextStyle(
                              fontSize: 12,
                              color: isUp ? AppColors.success : AppColors.error,
                              fontWeight: FontWeight.w500,
                            ),
                          ),
                        ],
                      ),
                    )
                  : const Text('-', style: TextStyle(fontSize: 12)),
              ],
            ),
          );
        }).toList(),
      ),
    );
  }
  
  Widget _buildProductionSummary(BuildContext context, List<Map<String, dynamic>> buildings) {
    if (buildings.isEmpty) {
      return _buildEmptyCard('Henüz üretim yapan işletmeniz yok');
    }
    
    // Group by type
    final Map<String, List<Map<String, dynamic>>> byType = {};
    for (final b in buildings) {
      final type = b['type']?.toString() ?? 'OTHER';
      byType.putIfAbsent(type, () => []).add(b);
    }
    
    final typeLabels = {
      'FARM': '${BuildingIcons.farmEmoji} Çiftlikler',
      'FACTORY': '${BuildingIcons.factoryEmoji} Fabrikalar',
      'MINE': '${BuildingIcons.mineEmoji} Madenler',
      'GARDEN': '${BuildingIcons.gardenEmoji} Bahçeler',
    };
    
    final typeRoutes = {
      'FARM': '/home/farms',
      'FACTORY': '/home/factories',
      'MINE': '/home/mines',
      'GARDEN': '/home/gardens',
    };
    
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.slate50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.slate200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: byType.entries.map((entry) {
          final type = entry.key;
          final typeBuildings = entry.value;
          final label = typeLabels[type] ?? type;
          final route = typeRoutes[type] ?? '/';
          
          // Calculate total production
          int totalProduction = 0;
          for (final b in typeBuildings) {
            final items = b['items'] as List<dynamic>? ?? [];
            for (final item in items) {
              totalProduction += (item['quantity'] ?? 0) as int;
            }
          }
          
          return InkWell(
            onTap: () => context.push(route),
            borderRadius: BorderRadius.circular(8),
            child: Padding(
              padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 4),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Row(
                    children: [
                      Text(
                        label,
                        style: const TextStyle(fontWeight: FontWeight.w500),
                      ),
                      const SizedBox(width: 4),
                      Icon(Icons.chevron_right, size: 16, color: AppColors.slate400),
                    ],
                  ),
                  Row(
                    children: [
                      Text(
                        '${typeBuildings.length} işletme',
                        style: TextStyle(color: AppColors.slate500),
                      ),
                      const SizedBox(width: 8),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                        decoration: BoxDecoration(
                          color: AppColors.primary.withOpacity(0.1),
                          borderRadius: BorderRadius.circular(4),
                        ),
                        child: Text(
                          '$totalProduction ürün',
                          style: TextStyle(
                            fontSize: 12,
                            color: AppColors.primary,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          );
        }).toList(),
      ),
    );
  }
  
  Widget _buildLoadingCard() {
    return Container(
      height: 100,
      decoration: BoxDecoration(
        color: AppColors.slate50,
        borderRadius: BorderRadius.circular(12),
      ),
      child: const Center(child: CircularProgressIndicator()),
    );
  }
  
  Widget _buildEmptyCard(String message) {
    return Container(
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: AppColors.slate50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.slate200),
      ),
      child: Center(
        child: Text(
          message,
          style: TextStyle(color: AppColors.slate400),
        ),
      ),
    );
  }

  Widget _buildStatsGrid(BuildContext context, Map<String, dynamic> stats) {
    final items = [
      _StatItem(
        emoji: BuildingIcons.shopEmoji,
        label: 'Dükkanlar',
        value: '${stats['shopCount'] ?? 0}',
        color: AppColors.info,
      ),
      _StatItem(
        emoji: BuildingIcons.farmEmoji,
        label: 'Çiftlikler',
        value: '${stats['farmCount'] ?? 0}',
        color: AppColors.success,
      ),
      _StatItem(
        emoji: BuildingIcons.factoryEmoji,
        label: 'Fabrikalar',
        value: '${stats['factoryCount'] ?? 0}',
        color: AppColors.warning,
      ),
      _StatItem(
        emoji: BuildingIcons.mineEmoji,
        label: 'Madenler',
        value: '${stats['mineCount'] ?? 0}',
        color: AppColors.slate600,
      ),
      _StatItem(
        emoji: BuildingIcons.gardenEmoji,
        label: 'Bahçeler',
        value: '${stats['gardenCount'] ?? 0}',
        color: AppColors.success,
      ),
      _StatItem(
        emoji: '📦',
        label: 'Envanter',
        value: '${stats['inventoryCount'] ?? 0}',
        color: AppColors.primary,
      ),
    ];

    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 2,
        mainAxisSpacing: 12,
        crossAxisSpacing: 12,
        childAspectRatio: 1.5,
      ),
      itemCount: items.length,
      itemBuilder: (context, index) {
        final item = items[index];
        return Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: AppColors.surface,
            borderRadius: BorderRadius.circular(12),
            border: Border.all(color: AppColors.slate200),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Row(
                children: [
                  Container(
                    padding: const EdgeInsets.all(8),
                    decoration: BoxDecoration(
                      color: item.color.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Text(item.emoji, style: const TextStyle(fontSize: 20)),
                  ),
                  const Spacer(),
                  Text(
                    item.value,
                    style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              Text(
                item.label,
                style: Theme.of(context).textTheme.bodySmall?.copyWith(
                  color: AppColors.slate500,
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildLoadingGrid() {
    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 2,
        mainAxisSpacing: 12,
        crossAxisSpacing: 12,
        childAspectRatio: 1.5,
      ),
      itemCount: 6,
      itemBuilder: (context, index) {
        return Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: AppColors.slate100,
            borderRadius: BorderRadius.circular(12),
          ),
          child: const Center(
            child: SizedBox(
              width: 20,
              height: 20,
              child: CircularProgressIndicator(strokeWidth: 2),
            ),
          ),
        );
      },
    );
  }

  Widget _buildQuickActions(BuildContext context) {
    final actions = [
      _QuickAction(
        emoji: '🏪',
        label: 'Dükkanlar',
        onTap: () => context.go('/home/shops'),
      ),
      _QuickAction(
        emoji: '🛒',
        label: 'Pazara Git',
        onTap: () => context.go('/home/market'),
      ),
      _QuickAction(
        emoji: '📦',
        label: 'Envanter',
        onTap: () => context.go('/home/inventory'),
      ),
      _QuickAction(
        emoji: '📊',
        label: 'Raporlar',
        onTap: () => context.go('/home/reports'),
      ),
    ];

    return Row(
      children: actions.map((action) {
        return Expanded(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 4),
            child: InkWell(
              onTap: action.onTap,
              borderRadius: BorderRadius.circular(12),
              child: Container(
                padding: const EdgeInsets.symmetric(vertical: 16),
                decoration: BoxDecoration(
                  color: AppColors.surface,
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: AppColors.slate200),
                ),
                child: Column(
                  children: [
                    Text(action.emoji, style: const TextStyle(fontSize: 24)),
                    const SizedBox(height: 8),
                    Text(
                      action.label,
                      style: Theme.of(context).textTheme.bodySmall,
                      textAlign: TextAlign.center,
                    ),
                  ],
                ),
              ),
            ),
          ),
        );
      }).toList(),
    );
  }
}

class _StatItem {
  final String emoji;
  final String label;
  final String value;
  final Color color;

  _StatItem({
    required this.emoji,
    required this.label,
    required this.value,
    required this.color,
  });
}

class _QuickAction {
  final String emoji;
  final String label;
  final VoidCallback onTap;

  _QuickAction({
    required this.emoji,
    required this.label,
    required this.onTap,
  });
}
