import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/theme/app_theme.dart';
import '../../core/api/api_client.dart';
import '../../core/widgets/currency_icon.dart';
import '../../core/widgets/product_emoji.dart';

// Inventory provider
final inventoryProvider = FutureProvider<List<Map<String, dynamic>>>((ref) async {
  final api = ApiClient();
  try {
    final response = await api.getInventory();
    final List data = response.data ?? [];
    return data.map<Map<String, dynamic>>((item) => Map<String, dynamic>.from(item)).toList();
  } catch (e) {
    print('Inventory error: $e');
    return [];
  }
});

class InventoryPage extends ConsumerWidget {
  const InventoryPage({super.key});

  String _getUnitTr(String? unit) {
    switch (unit?.toUpperCase()) {
      case 'KG':
        return 'kg';
      case 'LITRE':
      case 'LT':
        return 'lt';
      case 'ADET':
      case 'PIECE':
        return 'adet';
      default:
        return unit ?? 'adet';
    }
  }

  String _formatCurrency(num value) {
    final intValue = value.toInt();
    final str = intValue.toString();
    final buffer = StringBuffer();
    for (int i = 0; i < str.length; i++) {
      if (i > 0 && (str.length - i) % 3 == 0) {
        buffer.write('.');
      }
      buffer.write(str[i]);
    }
    return buffer.toString();
  }

  IconData _getProductIcon(String name) {
    final lower = name.toLowerCase();
    if (lower.contains('elma')) return Icons.apple;
    if (lower.contains('süt') || lower.contains('sut')) return Icons.water_drop;
    if (lower.contains('yumurta')) return Icons.egg;
    if (lower.contains('buğday') || lower.contains('bugday')) return Icons.grass;
    if (lower.contains('mısır') || lower.contains('misir')) return Icons.grass;
    if (lower.contains('domates')) return Icons.local_florist;
    if (lower.contains('patates')) return Icons.spa;
    if (lower.contains('peynir')) return Icons.breakfast_dining;
    if (lower.contains('ekmek')) return Icons.bakery_dining;
    if (lower.contains('et') || lower.contains('tavuk')) return Icons.restaurant;
    if (lower.contains('maden') || lower.contains('kömür') || lower.contains('demir')) return Icons.hardware;
    if (lower.contains('altın') || lower.contains('altin')) return Icons.diamond;
    return Icons.inventory_2;
  }

  Widget _buildStarRating(double score) {
    // Score is 0-100, convert to 0-5 stars
    final stars = (score / 20).clamp(0.0, 5.0);
    final fullStars = stars.floor();
    final hasHalfStar = (stars - fullStars) >= 0.5;
    
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: List.generate(5, (index) {
        if (index < fullStars) {
          return Icon(Icons.star, size: 14, color: AppColors.warning);
        } else if (index == fullStars && hasHalfStar) {
          return Icon(Icons.star_half, size: 14, color: AppColors.warning);
        } else {
          return Icon(Icons.star_border, size: 14, color: AppColors.slate300);
        }
      }),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final inventoryAsync = ref.watch(inventoryProvider);

    return RefreshIndicator(
      onRefresh: () async => ref.invalidate(inventoryProvider),
      child: CustomScrollView(
        slivers: [
          // Header
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Envanter',
                    style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    'Merkezi envanterinizdeki ürünleri görüntüleyin ve işletmelerinize transfer edin.',
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                      color: AppColors.slate500,
                    ),
                  ),
                ],
              ),
            ),
          ),

          // Content
          inventoryAsync.when(
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
                      child: _buildItemCard(context, items[index]),
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
              child: _buildErrorState(context, ref),
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
          Container(
            width: 64,
            height: 64,
            decoration: BoxDecoration(
              color: AppColors.slate100,
              shape: BoxShape.circle,
            ),
            child: Icon(
              Icons.inventory_2_outlined,
              size: 32,
              color: AppColors.slate400,
            ),
          ),
          const SizedBox(height: 16),
          Text(
            'Envanteriniz Boş',
            style: Theme.of(context).textTheme.titleLarge?.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 8),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 48),
            child: Text(
              'Pazardan ürün satın aldığınızda veya işletmenizden ürün transfer ettiğinizde buraya gelecektir.',
              textAlign: TextAlign.center,
              style: TextStyle(
                color: AppColors.slate500,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildErrorState(BuildContext context, WidgetRef ref) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.error_outline, size: 48, color: AppColors.error),
          const SizedBox(height: 16),
          Text('Envanter yüklenirken hata oluştu'),
          const SizedBox(height: 16),
          OutlinedButton.icon(
            onPressed: () => ref.invalidate(inventoryProvider),
            icon: const Icon(Icons.refresh),
            label: const Text('Tekrar Dene'),
          ),
        ],
      ),
    );
  }

  Widget _buildItemCard(BuildContext context, Map<String, dynamic> item) {
    final name = item['name'] ?? 'Ürün';
    final quantity = item['quantity'] ?? 0;
    final unit = item['unit'] ?? 'adet';
    final qualityScore = (item['qualityScore'] ?? 0).toDouble();
    final cost = item['cost'] ?? item['price'] ?? 0;

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.slate200),
      ),
      child: Column(
        children: [
          // Item Info Row
          Row(
            children: [
              // Product Emoji
              Container(
                width: 48,
                height: 48,
                decoration: BoxDecoration(
                  color: AppColors.primary.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Center(
                  child: ProductEmoji(productName: name, size: 28),
                ),
              ),
              const SizedBox(width: 12),
              
              // Name and Details
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      name,
                      style: Theme.of(context).textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Row(
                      children: [
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                          decoration: BoxDecoration(
                            color: AppColors.primary.withOpacity(0.1),
                            borderRadius: BorderRadius.circular(4),
                          ),
                          child: Text(
                            '$quantity ${_getUnitTr(unit)}',
                            style: TextStyle(
                              color: AppColors.primary,
                              fontWeight: FontWeight.w600,
                              fontSize: 12,
                            ),
                          ),
                        ),
                        const SizedBox(width: 8),
                        _buildStarRating(qualityScore),
                      ],
                    ),
                  ],
                ),
              ),
              
              // Price
              Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      CurrencyIcon(size: 14),
                      const SizedBox(width: 4),
                      Text(
                        _formatCurrency(cost),
                        style: TextStyle(
                          fontWeight: FontWeight.w600,
                          fontSize: 14,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ],
          ),
          
          const SizedBox(height: 12),
          const Divider(height: 1),
          const SizedBox(height: 12),
          
          // Action Buttons
          Row(
            children: [
              Expanded(
                child: ElevatedButton(
                  onPressed: () {
                    // TODO: Open sell modal
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('Pazara koyma özelliği yakında')),
                    );
                  },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.success,
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(vertical: 12),
                  ),
                  child: const Text('Pazara Koy'),
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: ElevatedButton(
                  onPressed: () {
                    // TODO: Open transfer modal
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('Transfer özelliği yakında')),
                    );
                  },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.primary,
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(vertical: 12),
                  ),
                  child: const Text('Transfer'),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
