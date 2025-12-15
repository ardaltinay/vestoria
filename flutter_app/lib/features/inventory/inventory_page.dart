import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/providers/auth_provider.dart';
import '../../core/theme/app_theme.dart';
import '../../core/api/api_client.dart';
import '../../core/widgets/currency_icon.dart';
import '../../core/widgets/product_emoji.dart';
import '../buildings/building_detail_page.dart'; // For buildingDetailProvider

// Inventory provider - filters out items with 0 or less quantity
final inventoryProvider = FutureProvider<List<Map<String, dynamic>>>((ref) async {
  final api = ApiClient();
  try {
    final response = await api.getInventory();
    final List data = response.data ?? [];
    return data
        .map<Map<String, dynamic>>((item) => Map<String, dynamic>.from(item))
        .where((item) => (item['quantity'] ?? 0) > 0) // Hide 0 quantity items
        .toList();
  } catch (e) {
    print('Inventory error: $e');
    return [];
  }
});

// Shops provider for transfer
final shopsProvider = FutureProvider<List<Map<String, dynamic>>>((ref) async {
  final api = ApiClient();
  try {
    final response = await api.getAllBuildings();
    final List data = response.data ?? [];
    return data
        .where((b) => b['type'] == 'SHOP')
        .map<Map<String, dynamic>>((b) => Map<String, dynamic>.from(b))
        .toList();
  } catch (e) {
    print('Shops error: $e');
    return [];
  }
});

class InventoryPage extends ConsumerStatefulWidget {
  const InventoryPage({super.key});

  @override
  ConsumerState<InventoryPage> createState() => _InventoryPageState();
}

class _InventoryPageState extends ConsumerState<InventoryPage> {
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

  Widget _buildStarRating(double score) {
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

  // Show modal for listing item on market
  Future<void> _showSellModal(BuildContext context, Map<String, dynamic> item) async {
    final name = item['name'] ?? 'Ürün';
    final maxQuantity = item['quantity'] ?? 0;
    final itemId = item['id']?.toString() ?? '';
    
    int quantity = 1;
    double price = (item['cost'] ?? item['price'] ?? 100).toDouble();
    
    final result = await showDialog<bool>(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setDialogState) => AlertDialog(
          title: Text('Pazara Koy: $name'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Mevcut: $maxQuantity ${_getUnitTr(item['unit'])}'),
              const SizedBox(height: 16),
              
              // Quantity
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
                        suffixText: _getUnitTr(item['unit']),
                      ),
                      controller: TextEditingController(text: quantity.toString()),
                      onChanged: (v) {
                        final parsed = int.tryParse(v);
                        if (parsed != null && parsed >= 1) {
                          setDialogState(() => quantity = parsed.clamp(1, maxQuantity) as int);
                        }
                      },
                    ),
                  ),
                  IconButton(
                    icon: Icon(Icons.add_circle_outline, color: AppColors.primary),
                    onPressed: quantity < maxQuantity ? () => setDialogState(() => quantity++) : null,
                  ),
                ],
              ),
              const SizedBox(height: 16),
              
              // Price per unit
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
                  hintText: 'Birim fiyat girin',
                ),
                controller: TextEditingController(text: price.toStringAsFixed(0)),
                onChanged: (v) => setDialogState(() => price = double.tryParse(v) ?? price),
              ),
              const SizedBox(height: 16),
              
              // Total
              Container(
                padding: EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: AppColors.success.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text('Toplam Değer:', style: TextStyle(fontWeight: FontWeight.w600)),
                    Row(
                      children: [
                        CurrencyIcon(size: 16),
                        SizedBox(width: 4),
                        Text(
                          _formatCurrency(quantity * price),
                          style: TextStyle(fontWeight: FontWeight.bold, color: AppColors.success),
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
              child: Text('İptal'),
            ),
            ElevatedButton(
              onPressed: () => Navigator.pop(context, true),
              style: ElevatedButton.styleFrom(backgroundColor: AppColors.success),
              child: Text('İlan Ver'),
            ),
          ],
        ),
      ),
    );
    
    if (result == true) {
      try {
        final api = ApiClient();
        await api.createMarketListing(itemId, quantity: quantity, price: price);
        ref.invalidate(inventoryProvider);
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('$name pazara konuldu!'), backgroundColor: Colors.green),
          );
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Hata: $e'), backgroundColor: Colors.red),
          );
        }
      }
    }
  }
  // Show modal for transferring item to shop
  Future<void> _showTransferModal(BuildContext context, Map<String, dynamic> item) async {
    final name = item['name'] ?? 'Ürün';
    final itemId = item['id']?.toString() ?? '';
    final maxQuantity = item['quantity'] ?? 0;
    
    // Load game data and shops
    List<Map<String, dynamic>> filteredShops = [];
    try {
      final api = ApiClient();
      
      // Load game data to get allowedItems per shop subType
      final gameDataResponse = await api.getGameData();
      final List gameData = gameDataResponse.data ?? [];
      
      // Load all buildings
      final buildingsResponse = await api.getAllBuildings();
      final List allBuildings = buildingsResponse.data ?? [];
      
      // Filter shops by checking if item name is in their allowedItems
      for (var building in allBuildings) {
        if (building['type'] != 'SHOP') continue;
        
        final subType = building['subType']?.toString();
        if (subType == null) continue;
        
        // Find the definition for this shop subType
        final definition = gameData.firstWhere(
          (d) => d['id'] == subType,
          orElse: () => null,
        );
        
        if (definition == null) continue;
        
        // Check if item is in allowedItems
        final List allowedItems = definition['allowedItems'] ?? [];
        final isAllowed = allowedItems.any((allowed) => 
          allowed.toString().trim().toLowerCase() == name.trim().toLowerCase()
        );
        
        if (isAllowed) {
          filteredShops.add(Map<String, dynamic>.from(building));
        }
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Dükkanlar yüklenemedi: $e'), backgroundColor: Colors.red),
        );
      }
      return;
    }
    
    if (filteredShops.isEmpty) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('$name için uygun dükkanınız yok. Bu ürünü satabilecek türde bir dükkan oluşturun.'), 
            backgroundColor: Colors.orange,
          ),
        );
      }
      return;
    }
    
    String? selectedShopId = filteredShops.first['id']?.toString();
    int quantity = 1;
    
    final result = await showDialog<bool>(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setDialogState) => AlertDialog(
          title: Text('Transfer: $name'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Mevcut: $maxQuantity ${_getUnitTr(item['unit'])}'),
              const SizedBox(height: 16),
              
              // Shop selection
              Text('Hedef Dükkan', style: TextStyle(fontWeight: FontWeight.w600)),
              const SizedBox(height: 8),
              DropdownButtonFormField<String>(
                value: selectedShopId,
                decoration: InputDecoration(
                  border: OutlineInputBorder(),
                  contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                ),
                items: filteredShops.map((shop) => DropdownMenuItem(
                  value: shop['id']?.toString(),
                  child: Text(shop['name'] ?? 'Dükkan'),
                )).toList(),
                onChanged: (v) => setDialogState(() => selectedShopId = v),
              ),
              const SizedBox(height: 16),
              
              // Quantity
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
                        suffixText: _getUnitTr(item['unit']),
                      ),
                      controller: TextEditingController(text: quantity.toString()),
                      onChanged: (v) {
                        final parsed = int.tryParse(v);
                        if (parsed != null && parsed >= 1) {
                          setDialogState(() => quantity = parsed.clamp(1, maxQuantity) as int);
                        }
                      },
                    ),
                  ),
                  IconButton(
                    icon: Icon(Icons.add_circle_outline, color: AppColors.primary),
                    onPressed: quantity < maxQuantity ? () => setDialogState(() => quantity++) : null,
                  ),
                ],
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context, false),
              child: Text('İptal'),
            ),
            ElevatedButton(
              onPressed: () => Navigator.pop(context, true),
              style: ElevatedButton.styleFrom(backgroundColor: AppColors.primary),
              child: Text('Transfer Et'),
            ),
          ],
        ),
      ),
    );
    
    if (result == true && selectedShopId != null && itemId.isNotEmpty) {
      try {
        final api = ApiClient();
        await api.transferFromInventory(itemId, selectedShopId!, quantity);
        
        // Refresh inventory and user balance (in case of fees/sync)
        if (mounted) {
          ref.invalidate(inventoryProvider);
          ref.read(authProvider.notifier).refreshUser();
          
          // Also invalidate the specific building detail provider so it shows the new item immediately
          ref.invalidate(buildingDetailProvider(selectedShopId!));
          
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('$quantity ${_getUnitTr(item['unit'])} $name transfer edildi!'), 
              backgroundColor: Colors.green
            ),
          );
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(ApiClient.getErrorMessage(e)), 
              backgroundColor: Colors.red,
              duration: const Duration(seconds: 4), // Longer duration to read error
            ),
          );
        }
      }
    }
  }

  @override
  Widget build(BuildContext context) {
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

  Widget _buildErrorState(BuildContext context) {
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
                  onPressed: () => _showSellModal(context, item),
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
                  onPressed: () => _showTransferModal(context, item),
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
