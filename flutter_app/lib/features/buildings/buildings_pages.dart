import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../core/theme/app_theme.dart';
import '../../core/api/api_client.dart';
import '../../data/models/building.dart';

// Farms provider
final farmsProvider = FutureProvider<List<Building>>((ref) async {
  final api = ApiClient();
  final data = await api.getFarms();
  return data.map((json) => Building.fromJson(json)).toList();
});

class FarmsPage extends ConsumerWidget {
  const FarmsPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final farmsAsync = ref.watch(farmsProvider);

    return _BuildingListPage(
      title: 'Çiftliklerim',
      icon: Icons.agriculture,
      iconColor: AppColors.success,
      asyncData: farmsAsync,
      onRefresh: () async => ref.invalidate(farmsProvider),
      emptyTitle: 'Henüz çiftliğiniz yok',
      emptySubtitle: 'İlk çiftliğinizi oluşturun',
    );
  }
}

// Factories provider
final factoriesProvider = FutureProvider<List<Building>>((ref) async {
  final api = ApiClient();
  final data = await api.getFactories();
  return data.map((json) => Building.fromJson(json)).toList();
});

class FactoriesPage extends ConsumerWidget {
  const FactoriesPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final factoriesAsync = ref.watch(factoriesProvider);

    return _BuildingListPage(
      title: 'Fabrikalarım',
      icon: Icons.factory,
      iconColor: AppColors.warning,
      asyncData: factoriesAsync,
      onRefresh: () async => ref.invalidate(factoriesProvider),
      emptyTitle: 'Henüz fabrikanız yok',
      emptySubtitle: 'İlk fabrikanızı oluşturun',
    );
  }
}

// Mines provider
final minesProvider = FutureProvider<List<Building>>((ref) async {
  final api = ApiClient();
  final data = await api.getMines();
  return data.map((json) => Building.fromJson(json)).toList();
});

class MinesPage extends ConsumerWidget {
  const MinesPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final minesAsync = ref.watch(minesProvider);

    return _BuildingListPage(
      title: 'Madenlerim',
      icon: Icons.hardware,
      iconColor: AppColors.slate600,
      asyncData: minesAsync,
      onRefresh: () async => ref.invalidate(minesProvider),
      emptyTitle: 'Henüz madeniniz yok',
      emptySubtitle: 'İlk madeninizi oluşturun',
    );
  }
}

// Gardens provider
final gardensProvider = FutureProvider<List<Building>>((ref) async {
  final api = ApiClient();
  final data = await api.getGardens();
  return data.map((json) => Building.fromJson(json)).toList();
});

class GardensPage extends ConsumerWidget {
  const GardensPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final gardensAsync = ref.watch(gardensProvider);

    return _BuildingListPage(
      title: 'Bahçelerim',
      icon: Icons.park,
      iconColor: AppColors.success,
      asyncData: gardensAsync,
      onRefresh: () async => ref.invalidate(gardensProvider),
      emptyTitle: 'Henüz bahçeniz yok',
      emptySubtitle: 'İlk bahçenizi oluşturun',
    );
  }
}

// Reusable building list page component
class _BuildingListPage extends StatelessWidget {
  final String title;
  final IconData icon;
  final Color iconColor;
  final AsyncValue<List<Building>> asyncData;
  final Future<void> Function() onRefresh;
  final String emptyTitle;
  final String emptySubtitle;

  const _BuildingListPage({
    required this.title,
    required this.icon,
    required this.iconColor,
    required this.asyncData,
    required this.onRefresh,
    required this.emptyTitle,
    required this.emptySubtitle,
  });

  @override
  Widget build(BuildContext context) {
    return RefreshIndicator(
      onRefresh: onRefresh,
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
                          title,
                          style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        const SizedBox(height: 4),
                        asyncData.when(
                          data: (items) => Text(
                            '${items.length} adet',
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
                    onPressed: () {
                      // TODO: Navigate to create
                    },
                    icon: const Icon(Icons.add, size: 18),
                    label: const Text('Yeni'),
                  ),
                ],
              ),
            ),
          ),

          // List
          asyncData.when(
            data: (items) {
              if (items.isEmpty) {
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
                      child: _BuildingCard(
                        building: items[index],
                        icon: icon,
                        iconColor: iconColor,
                      ),
                    ),
                    childCount: items.length,
                  ),
                ),
              );
            },
            loading: () => const SliverFillRemaining(
              child: Center(child: CircularProgressIndicator()),
            ),
            error: (error, stack) => SliverFillRemaining(
              child: _buildErrorState(context, error),
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
          Icon(icon, size: 80, color: AppColors.slate300),
          const SizedBox(height: 16),
          Text(
            emptyTitle,
            style: Theme.of(context).textTheme.titleLarge?.copyWith(
              color: AppColors.slate500,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            emptySubtitle,
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
              color: AppColors.slate400,
            ),
          ),
          const SizedBox(height: 24),
          ElevatedButton.icon(
            onPressed: () {},
            icon: const Icon(Icons.add),
            label: const Text('Oluştur'),
          ),
        ],
      ),
    );
  }

  Widget _buildErrorState(BuildContext context, Object error) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.error_outline, size: 60, color: AppColors.error),
          const SizedBox(height: 16),
          Text('Bir hata oluştu', style: Theme.of(context).textTheme.titleLarge),
          const SizedBox(height: 24),
          OutlinedButton.icon(
            onPressed: onRefresh,
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
  final IconData icon;
  final Color iconColor;

  const _BuildingCard({
    required this.building,
    required this.icon,
    required this.iconColor,
  });

  @override
  Widget build(BuildContext context) {
    // Determine route based on building type
    String detailRoute;
    switch (building.type.toUpperCase()) {
      case 'FARM':
        detailRoute = '/farms/${building.id}';
        break;
      case 'FACTORY':
        detailRoute = '/factories/${building.id}';
        break;
      case 'MINE':
        detailRoute = '/mines/${building.id}';
        break;
      case 'GARDEN':
        detailRoute = '/gardens/${building.id}';
        break;
      default:
        detailRoute = '/shops/${building.id}';
    }
    
    return InkWell(
      onTap: () => context.push(detailRoute),
      borderRadius: BorderRadius.circular(12),
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: AppColors.surface,
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: AppColors.slate200),
        ),
        child: Row(
          children: [
            Container(
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                color: iconColor.withOpacity(0.1),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Icon(icon, color: iconColor, size: 24),
            ),
            const SizedBox(width: 12),
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
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
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
                  if (building.subType != null) ...[
                    const SizedBox(height: 4),
                    Text(
                      building.subTypeTr,
                      style: Theme.of(context).textTheme.bodySmall?.copyWith(
                        color: AppColors.slate500,
                      ),
                    ),
                  ],
                ],
              ),
            ),
            Icon(Icons.chevron_right, color: AppColors.slate400),
          ],
        ),
      ),
    );
  }
}
