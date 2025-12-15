import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/theme/app_theme.dart';
import '../../core/api/api_client.dart';
import '../../core/widgets/currency_icon.dart';
import '../../core/widgets/product_emoji.dart';
import '../../core/providers/auth_provider.dart';
import '../../core/services/websocket_service.dart';
import '../inventory/inventory_page.dart'; // For inventoryProvider

// Marketplace listings provider
final marketplaceProvider = FutureProvider<List<Map<String, dynamic>>>((ref) async {
  final api = ApiClient();
  try {
    final response = await api.getMarketListings();
    // Backend returns Page object with content array
    final dynamic data = response.data;
    List contentList;
    if (data is Map && data['content'] != null) {
      // Page response format: {content: [...], pageable: ..., totalElements: ...}
      contentList = data['content'] as List;
    } else if (data is List) {
      // Direct list format
      contentList = data;
    } else {
      contentList = [];
    }
    return contentList.map<Map<String, dynamic>>((item) => Map<String, dynamic>.from(item)).toList();
  } catch (e) {
    print('Marketplace error: $e');
    return [];
  }
});

class MarketplacePage extends ConsumerStatefulWidget {
  const MarketplacePage({super.key});

  @override
  ConsumerState<MarketplacePage> createState() => _MarketplacePageState();
}

class _MarketplacePageState extends ConsumerState<MarketplacePage> {
  final TextEditingController _searchController = TextEditingController();
  String _searchQuery = '';
  Map<String, dynamic>? _lastUpdate;

  @override
  void initState() {
    super.initState();
    // Connect to WebSocket
    WidgetsBinding.instance.addPostFrameCallback((_) {
      ref.read(marketWebSocketProvider.notifier).connect();
    });
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
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

  String _getUnitTr(String? unit) {
    switch (unit?.toUpperCase()) {
      case 'KG':
        return 'kg';
      case 'LITRE':
      case 'LITER':
      case 'LT':
        return 'lt';
      case 'ADET':
      case 'PIECE':
        return 'adet';
      default:
        return unit?.toLowerCase() ?? 'adet';
    }
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
          return Icon(Icons.star, size: 12, color: AppColors.warning);
        } else if (index == fullStars && hasHalfStar) {
          return Icon(Icons.star_half, size: 12, color: AppColors.warning);
        } else {
          return Icon(Icons.star_border, size: 12, color: AppColors.slate300);
        }
      }),
    );
  }

  List<Map<String, dynamic>> _filterListings(List<Map<String, dynamic>> listings) {
    if (_searchQuery.isEmpty) return listings;
    final query = _searchQuery.toLowerCase();
    return listings.where((listing) {
      final itemName = (listing['itemName'] ?? '').toString().toLowerCase();
      final sellerName = (listing['sellerUsername'] ?? '').toString().toLowerCase();
      return itemName.contains(query) || sellerName.contains(query);
    }).toList();
  }

  Future<void> _handleBuy(Map<String, dynamic> listing) async {
    final itemName = listing['itemName'] ?? 'Ürün';
    final maxQuantity = listing['quantity'] ?? 1;
    final pricePerUnit = (listing['price'] ?? 0).toDouble();
    final sellerUsername = listing['sellerUsername'] ?? '';
    final isVestoria = sellerUsername.toString().toLowerCase() == 'vestoria';
    
    int quantity = 1;
    
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setDialogState) => AlertDialog(
          title: Text('Satın Al: $itemName'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Seller info
              Row(
                children: [
                  Icon(Icons.person_outline, size: 16, color: AppColors.slate500),
                  SizedBox(width: 4),
                  Text('Satıcı: $sellerUsername', style: TextStyle(color: AppColors.slate500)),
                ],
              ),
              const SizedBox(height: 4),
              Row(
                children: [
                  Icon(Icons.inventory_2_outlined, size: 16, color: AppColors.slate500),
                  SizedBox(width: 4),
                  Text('Stok: ${isVestoria ? '∞' : maxQuantity}', style: TextStyle(color: AppColors.slate500)),
                ],
              ),
              const SizedBox(height: 16),
              
              // Price per unit
              Container(
                padding: EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: AppColors.slate100,
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text('Birim Fiyat:', style: TextStyle(fontWeight: FontWeight.w500)),
                    Row(
                      children: [
                        CurrencyIcon(size: 14),
                        SizedBox(width: 4),
                        Text(
                          _formatCurrency(pricePerUnit),
                          style: TextStyle(fontWeight: FontWeight.bold),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 16),
              
              // Quantity selection
              Text('Miktar', style: TextStyle(fontWeight: FontWeight.w600)),
              const SizedBox(height: 8),
              Row(
                children: [
                  IconButton(
                    icon: Icon(Icons.remove_circle_outline, color: AppColors.primary),
                    onPressed: quantity > 1 ? () => setDialogState(() => quantity--) : null,
                  ),
                  Expanded(
                    child: TextField(
                      keyboardType: TextInputType.number,
                      textAlign: TextAlign.center,
                      decoration: InputDecoration(
                        contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                        border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
                        hintText: 'Miktar',
                        suffixText: _getUnitTr(listing['unit']),
                      ),
                      controller: TextEditingController(text: quantity.toString()),
                      onChanged: (v) {
                        final parsed = int.tryParse(v);
                        if (parsed != null && parsed >= 1) {
                          final max = isVestoria ? 10000 : maxQuantity;
                          setDialogState(() => quantity = parsed.clamp(1, max) as int);
                        }
                      },
                    ),
                  ),
                  IconButton(
                    icon: Icon(Icons.add_circle_outline, color: AppColors.primary),
                    onPressed: (isVestoria || quantity < maxQuantity) ? () => setDialogState(() => quantity++) : null,
                  ),
                ],
              ),
              const SizedBox(height: 16),
              
              // Total price
              Container(
                padding: EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: AppColors.success.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: AppColors.success.withOpacity(0.3)),
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text('Toplam Tutar:', style: TextStyle(fontWeight: FontWeight.w600, fontSize: 16)),
                    Row(
                      children: [
                        CurrencyIcon(size: 18),
                        SizedBox(width: 4),
                        Text(
                          _formatCurrency(quantity * pricePerUnit),
                          style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18, color: AppColors.success),
                        ),
                      ],
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
              onPressed: () => Navigator.pop(context, true),
              icon: Icon(Icons.shopping_cart),
              label: Text('Satın Al'),
              style: ElevatedButton.styleFrom(backgroundColor: AppColors.primary),
            ),
          ],
        ),
      ),
    );

    if (confirmed == true) {
      try {
        final api = ApiClient();
        await api.buyMarketListing(listing['id'].toString(), quantity: quantity);
        
        // Refresh marketplace, and user state (balance & inventory)
        ref.invalidate(marketplaceProvider);
        ref.read(authProvider.notifier).refreshUser();
        // Also invalidate inventory so it's fresh when user navigates there
        ref.invalidate(inventoryProvider as ProviderOrFamily); 

        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('$quantity adet $itemName satın alındı!'),
              backgroundColor: Colors.green,
            ),
          );
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(ApiClient.getErrorMessage(e)),
              backgroundColor: Colors.red,
              duration: const Duration(seconds: 4),
            ),
          );
        }
      }
    }
  }

  Future<void> _handleCancel(Map<String, dynamic> listing) async {
    final itemName = listing['itemName'] ?? 'Ürün';
    final quantity = listing['quantity'] ?? 0;
    
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: Row(
          children: [
            Icon(Icons.warning_amber_rounded, color: AppColors.warning),
            const SizedBox(width: 8),
            const Text('İlanı Kaldır'),
          ],
        ),
        content: Text(
          '$itemName ($quantity adet) ilanını pazardan kaldırmak istediğinize emin misiniz?\n\nÜrünler envanterinize geri dönecektir.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('İptal'),
          ),
          ElevatedButton(
            onPressed: () => Navigator.pop(context, true),
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.error,
              foregroundColor: Colors.white,
            ),
            child: const Text('Kaldır'),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      try {
        final api = ApiClient();
        await api.cancelMarketListing(listing['id'].toString());
        
        ref.invalidate(marketplaceProvider);
        // Invalidate inventory so canceled items appear immediately
        ref.invalidate(inventoryProvider as ProviderOrFamily);

        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('$itemName ilanı kaldırıldı'),
              backgroundColor: Colors.green,
            ),
          );
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(ApiClient.getErrorMessage(e)),
              backgroundColor: Colors.red,
              duration: const Duration(seconds: 4),
            ),
          );
        }
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final listingsAsync = ref.watch(marketplaceProvider);
    final currentUser = ref.watch(authProvider).user;
    
    // Listen for WebSocket updates and auto-refresh
    final wsState = ref.watch(marketWebSocketProvider);
    if (wsState.lastUpdate != null && wsState.lastUpdate != _lastUpdate) {
      _lastUpdate = wsState.lastUpdate;
      // Auto-refresh when market update received
      WidgetsBinding.instance.addPostFrameCallback((_) {
        ref.invalidate(marketplaceProvider);
      });
    }

    return RefreshIndicator(
      onRefresh: () async => ref.invalidate(marketplaceProvider),
      child: CustomScrollView(
        slivers: [
          // Header with Search
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Expanded(
                        child: Text(
                          'Pazar Yeri',
                          style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                      ElevatedButton.icon(
                        onPressed: () {
                          // TODO: Open sell modal
                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(content: Text('İlan verme özelliği yakında')),
                          );
                        },
                        icon: const Icon(Icons.add, size: 18),
                        label: const Text('İlan Ver'),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  
                  // Search Bar
                  TextField(
                    controller: _searchController,
                    onChanged: (value) => setState(() => _searchQuery = value),
                    decoration: InputDecoration(
                      hintText: 'Ürün ara...',
                      prefixIcon: const Icon(Icons.search),
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(12),
                        borderSide: BorderSide(color: AppColors.slate200),
                      ),
                      enabledBorder: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(12),
                        borderSide: BorderSide(color: AppColors.slate200),
                      ),
                      focusedBorder: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(12),
                        borderSide: BorderSide(color: AppColors.primary, width: 2),
                      ),
                      filled: true,
                      fillColor: AppColors.surface,
                      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                    ),
                  ),
                ],
              ),
            ),
          ),

          // Content
          listingsAsync.when(
            data: (listings) {
              final filtered = _filterListings(listings);
              if (filtered.isEmpty) {
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
                      child: _buildListingCard(context, filtered[index], currentUser?.username),
                    ),
                    childCount: filtered.length,
                  ),
                ),
              );
            },
            loading: () => const SliverFillRemaining(
              child: Center(child: CircularProgressIndicator()),
            ),
            error: (error, stack) => SliverFillRemaining(
              child: _buildErrorState(context),
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
              Icons.shopping_cart_outlined,
              size: 32,
              color: AppColors.slate400,
            ),
          ),
          const SizedBox(height: 16),
          Text(
            'Henüz İlan Yok',
            style: Theme.of(context).textTheme.titleLarge?.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 8),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 48),
            child: Text(
              'Şu anda pazarda aktif bir ilan bulunmuyor. İlk ilanı siz verin!',
              textAlign: TextAlign.center,
              style: TextStyle(
                color: AppColors.slate500,
              ),
            ),
          ),
          const SizedBox(height: 24),
          ElevatedButton.icon(
            onPressed: () {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('İlan verme özelliği yakında')),
              );
            },
            icon: const Icon(Icons.add),
            label: const Text('Hemen İlan Ver'),
          ),
        ],
      ),
    );
  }

  Widget _buildErrorState(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.error_outline, size: 48, color: AppColors.error),
          const SizedBox(height: 16),
          Text('Pazar yüklenirken hata oluştu'),
          const SizedBox(height: 16),
          OutlinedButton.icon(
            onPressed: () => ref.invalidate(marketplaceProvider),
            icon: const Icon(Icons.refresh),
            label: const Text('Tekrar Dene'),
          ),
        ],
      ),
    );
  }

  Widget _buildListingCard(BuildContext context, Map<String, dynamic> listing, String? currentUsername) {
    final itemName = listing['itemName'] ?? 'Ürün';
    final sellerUsername = listing['sellerUsername'] ?? 'Satıcı';
    final quantity = listing['quantity'] ?? 0;
    final qualityScore = (listing['qualityScore'] ?? 0).toDouble();
    final price = listing['price'] ?? 0;
    final isOwnListing = sellerUsername == currentUsername;
    final isVestoria = sellerUsername.toString().toLowerCase() == 'vestoria';

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
                  child: ProductEmoji(productName: itemName, size: 28),
                ),
              ),
              const SizedBox(width: 12),
              
              // Name and Seller
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      itemName,
                      style: Theme.of(context).textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      'Satıcı: $sellerUsername',
                      style: TextStyle(
                        color: AppColors.slate500,
                        fontSize: 12,
                      ),
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
                      CurrencyIcon(size: 16),
                      const SizedBox(width: 4),
                      Text(
                        _formatCurrency(price),
                        style: TextStyle(
                          fontWeight: FontWeight.bold,
                          fontSize: 16,
                          color: AppColors.success,
                        ),
                      ),
                    ],
                  ),
                  Text(
                    '/ birim',
                    style: TextStyle(
                      color: AppColors.slate400,
                      fontSize: 10,
                    ),
                  ),
                ],
              ),
            ],
          ),
          
          const SizedBox(height: 12),
          const Divider(height: 1),
          const SizedBox(height: 12),
          
          // Bottom Row
          Row(
            children: [
              // Quantity Badge
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                decoration: BoxDecoration(
                  color: AppColors.slate100,
                  borderRadius: BorderRadius.circular(6),
                ),
                child: Text(
                  isVestoria ? '∞' : '$quantity ${_getUnitTr(listing['unit'])}',
                  style: TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.w600,
                    color: AppColors.slate600,
                  ),
                ),
              ),
              const SizedBox(width: 12),
              
              // Star Rating
              _buildStarRating(qualityScore),
              
              const Spacer(),
              
              // Action Button
              if (isOwnListing)
                OutlinedButton.icon(
                  onPressed: () => _handleCancel(listing),
                  style: OutlinedButton.styleFrom(
                    foregroundColor: AppColors.error,
                    side: BorderSide(color: AppColors.error.withOpacity(0.3)),
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                  ),
                  icon: const Icon(Icons.close, size: 16),
                  label: const Text('Kaldır'),
                )
              else
                ElevatedButton.icon(
                  onPressed: () => _handleBuy(listing),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.primary,
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                  ),
                  icon: const Icon(Icons.shopping_cart, size: 16),
                  label: const Text('Satın Al'),
                ),
            ],
          ),
        ],
      ),
    );
  }
}
