import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../core/theme/app_theme.dart';
import '../../core/providers/auth_provider.dart';
import '../../core/api/api_client.dart';
import '../../core/widgets/currency_icon.dart';
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
        ref.invalidate(dashboardStatsProvider);
        ref.invalidate(authProvider);
      },
      child: SingleChildScrollView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Welcome Card
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  colors: [AppColors.primary, AppColors.primaryDark],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
                borderRadius: BorderRadius.circular(16),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Hoş geldiniz, ${user?.username ?? 'Oyuncu'}! 👋',
                    style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                      color: Colors.white,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'İş imparatorluğunuzu büyütmeye devam edin',
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                      color: Colors.white.withOpacity(0.9),
                    ),
                  ),
                  const SizedBox(height: 16),
                  // Balance display
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                    decoration: BoxDecoration(
                      color: Colors.white.withOpacity(0.2),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        const CurrencyIcon(size: 18),
                        const SizedBox(width: 8),
                        Text(
                          formatCurrency(user?.balance ?? 0),
                          style: const TextStyle(
                            color: Colors.white,
                            fontWeight: FontWeight.bold,
                            fontSize: 18,
                          ),
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

  Widget _buildStatsGrid(BuildContext context, Map<String, dynamic> stats) {
    final items = [
      _StatItem(
        emoji: '🏪',
        label: 'Dükkanlar',
        value: '${stats['shopCount'] ?? 0}',
        color: AppColors.info,
      ),
      _StatItem(
        emoji: '🌾',
        label: 'Çiftlikler',
        value: '${stats['farmCount'] ?? 0}',
        color: AppColors.success,
      ),
      _StatItem(
        emoji: '🏭',
        label: 'Fabrikalar',
        value: '${stats['factoryCount'] ?? 0}',
        color: AppColors.warning,
      ),
      _StatItem(
        emoji: '⛏️',
        label: 'Madenler',
        value: '${stats['mineCount'] ?? 0}',
        color: AppColors.slate600,
      ),
      _StatItem(
        emoji: '🌸',
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
