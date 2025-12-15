import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../core/theme/app_theme.dart';
import '../../core/api/api_client.dart';
import '../../data/models/building.dart';
import '../../core/widgets/currency_icon.dart';
import '../../core/widgets/building_icons.dart';

// Shops provider
final shopsProvider = FutureProvider<List<Building>>((ref) async {
  final api = ApiClient();
  final data = await api.getShops();
  return data.map((json) => Building.fromJson(json)).toList();
});

class ShopsPage extends ConsumerWidget {
  const ShopsPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final shopsAsync = ref.watch(shopsProvider);

    return RefreshIndicator(
      onRefresh: () async {
        ref.invalidate(shopsProvider);
      },
      child: CustomScrollView(
        slivers: [
          // Header
          SliverToBoxAdapter(
            child: Container(
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Dükkanlarım',
                          style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        const SizedBox(height: 4),
                        shopsAsync.when(
                          data: (shops) => Text(
                            '${shops.length} dükkan',
                            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                              color: AppColors.slate500,
                            ),
                          ),
                          loading: () => Text(
                            'Yükleniyor...',
                            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                              color: AppColors.slate500,
                            ),
                          ),
                          error: (_, __) => Text(
                            'Hata oluştu',
                            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                              color: AppColors.error,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                  ElevatedButton.icon(
                    onPressed: () async {
                      final result = await context.push('/create-building/SHOP');
                      if (result == true) {
                        ref.invalidate(shopsProvider);
                      }
                    },
                    icon: const Icon(Icons.add, size: 18),
                    label: const Text('Yeni'),
                  ),
                ],
              ),
            ),
          ),

          // Shops List
          shopsAsync.when(
            data: (shops) {
              if (shops.isEmpty) {
                return SliverFillRemaining(
                  child: _buildEmptyState(context),
                );
              }
              return SliverPadding(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                sliver: SliverList(
                  delegate: SliverChildBuilderDelegate(
                    (context, index) => Padding(
                      padding: const EdgeInsets.only(bottom: 12),
                      child: _BuildingCard(building: shops[index]),
                    ),
                    childCount: shops.length,
                  ),
                ),
              );
            },
            loading: () => SliverFillRemaining(
              child: _buildLoadingState(),
            ),
            error: (error, stack) => SliverFillRemaining(
              child: _buildErrorState(context, ref, error),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildEmptyState(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Text(BuildingIcons.shopEmoji, style: const TextStyle(fontSize: 64)),
          const SizedBox(height: 16),
          Text(
            'Henüz dükkanınız yok',
            style: Theme.of(context).textTheme.titleLarge?.copyWith(
              color: AppColors.slate500,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'İlk dükkanınızı oluşturun',
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
              color: AppColors.slate400,
            ),
          ),
          const SizedBox(height: 24),
          ElevatedButton.icon(
            onPressed: () {
              // TODO: Navigate to create shop
            },
            icon: const Icon(Icons.add),
            label: const Text('Dükkan Oluştur'),
          ),
        ],
      ),
    );
  }

  Widget _buildLoadingState() {
    return const Center(
      child: CircularProgressIndicator(),
    );
  }

  Widget _buildErrorState(BuildContext context, WidgetRef ref, Object error) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.error_outline, size: 60, color: AppColors.error),
          const SizedBox(height: 16),
          Text(
            'Bir hata oluştu',
            style: Theme.of(context).textTheme.titleLarge,
          ),
          const SizedBox(height: 8),
          Text(
            error.toString(),
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
              color: AppColors.slate500,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 24),
          OutlinedButton.icon(
            onPressed: () => ref.invalidate(shopsProvider),
            icon: const Icon(Icons.refresh),
            label: const Text('Tekrar Dene'),
          ),
        ],
      ),
    );
  }
}

class _BuildingCard extends StatelessWidget {
  final Building building;

  const _BuildingCard({required this.building});

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: () {
        context.push('/shops/${building.id}');
      },
      borderRadius: BorderRadius.circular(12),
      child: Container(
        padding: const EdgeInsets.all(16),
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
                // Icon
                Container(
                  width: 48,
                  height: 48,
                  decoration: BoxDecoration(
                    color: AppColors.info.withOpacity(0.1),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Center(
                    child: Text(
                      BuildingIcons.shopEmoji,
                      style: const TextStyle(fontSize: 24),
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                
                // Info
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        building.name,
                        style: Theme.of(context).textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Row(
                        children: [
                          Container(
                            padding: const EdgeInsets.symmetric(
                              horizontal: 6, 
                              vertical: 2,
                            ),
                            decoration: BoxDecoration(
                              color: AppColors.primary.withOpacity(0.1),
                              borderRadius: BorderRadius.circular(4),
                            ),
                            child: Text(
                              'LVL ${building.tierLevel}',
                              style: Theme.of(context).textTheme.labelSmall?.copyWith(
                                color: AppColors.primary,
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                          ),
                          const SizedBox(width: 8),
                          if (building.subType != null)
                            Text(
                              building.subTypeTr,
                              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                                color: AppColors.slate500,
                              ),
                            ),
                        ],
                      ),
                    ],
                  ),
                ),
                
                // Arrow
                Icon(
                  Icons.chevron_right,
                  color: AppColors.slate400,
                ),
              ],
            ),
            
            // Stock bar (if applicable)
            if (building.currentStock != null && building.maxStock != null) ...[
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(
                              'Stok',
                              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                                color: AppColors.slate500,
                              ),
                            ),
                            Text(
                              building.stockText,
                              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                                color: AppColors.slate600,
                                fontWeight: FontWeight.w500,
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 4),
                        LinearProgressIndicator(
                          value: building.stockPercentage,
                          backgroundColor: AppColors.slate200,
                          valueColor: AlwaysStoppedAnimation(
                            building.stockPercentage > 0.7 
                              ? AppColors.success 
                              : building.stockPercentage > 0.3 
                                ? AppColors.warning 
                                : AppColors.error,
                          ),
                          minHeight: 6,
                          borderRadius: BorderRadius.circular(3),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ],
          ],
        ),
      ),
    );
  }
}
