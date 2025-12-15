import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../core/theme/app_theme.dart';
import '../../core/api/api_client.dart';
import '../../core/widgets/currency_icon.dart';
import '../../core/utils/translations.dart';

// Building configs provider
final buildingConfigsProvider = FutureProvider<Map<String, dynamic>>((ref) async {
  final api = ApiClient();
  try {
    final response = await api.getBuildingConfigs();
    print('Building configs API response: ${response.data}');
    
    // API returns List of configs, transform to nested Map {TYPE: {TIER: {...}}}
    final List configList = response.data ?? [];
    final Map<String, dynamic> nestedConfigs = {};
    
    for (final config in configList) {
      final type = config['type']?.toString() ?? '';
      final tier = config['tier']?.toString() ?? '';
      
      if (type.isNotEmpty && tier.isNotEmpty) {
        nestedConfigs[type] ??= {};
        nestedConfigs[type][tier] = Map<String, dynamic>.from(config);
      }
    }
    
    print('Transformed configs: $nestedConfigs');
    return nestedConfigs;
  } catch (e) {
    print('Building configs error: $e');
    return {};
  }
});

// Game data provider for subtypes
final gameDataProvider = FutureProvider<List<Map<String, dynamic>>>((ref) async {
  final api = ApiClient();
  try {
    final response = await api.getGameData();
    final List data = response.data ?? [];
    return data.map((e) => Map<String, dynamic>.from(e)).toList();
  } catch (e) {
    print('Game data error: $e');
    return [];
  }
});

class CreateBuildingWizard extends ConsumerStatefulWidget {
  final String buildingType;
  
  const CreateBuildingWizard({
    super.key,
    required this.buildingType,
  });

  @override
  ConsumerState<CreateBuildingWizard> createState() => _CreateBuildingWizardState();
}

class _CreateBuildingWizardState extends ConsumerState<CreateBuildingWizard> {
  int _step = 1;
  String? _selectedSubType;
  String? _selectedTier;
  String _buildingName = '';
  bool _isSubmitting = false;
  bool _showNameError = false;
  
  bool get isShop => widget.buildingType.toUpperCase() == 'SHOP';
  
  int get totalSteps => isShop ? 3 : 2;
  
  int get displayStep => isShop ? _step : _step - 1;
  
  @override
  void initState() {
    super.initState();
    // For non-shop buildings, start at step 2 (tier selection)
    // _selectedSubType stays null for non-SHOP, which is correct for backend
    if (!isShop) {
      _step = 2;
    }
  }

  String get buildingTypeName => Translations.getBuildingTypeTr(widget.buildingType);

  List<Map<String, dynamic>> _getSubTypeOptions(List<Map<String, dynamic>> gameData) {
    return gameData.where((item) => 
      item['type']?.toString().toUpperCase() == widget.buildingType.toUpperCase()
    ).toList();
  }

  Map<String, dynamic>? _getConfig(Map<String, dynamic> configs, String tier) {
    final typeConfigs = configs[widget.buildingType.toUpperCase()];
    if (typeConfigs == null) return null;
    return typeConfigs is Map ? Map<String, dynamic>.from(typeConfigs[tier] ?? {}) : null;
  }

  int _calculateTotalCost(Map<String, dynamic> configs) {
    final config = _getConfig(configs, _selectedTier ?? 'SMALL');
    return (config?['cost'] ?? 0) as int;
  }

  String _getTierLabel(String tier) {
    switch (tier) {
      case 'SMALL': return 'Küçük';
      case 'MEDIUM': return 'Orta';
      case 'LARGE': return 'Büyük';
      default: return tier;
    }
  }

  String _getTierBadge(String tier) {
    switch (tier) {
      case 'SMALL': return 'Başlangıç';
      case 'MEDIUM': return 'Popüler';
      case 'LARGE': return 'Pro';
      default: return '';
    }
  }

  Color _getTierBadgeColor(String tier) {
    switch (tier) {
      case 'SMALL': return AppColors.slate500;
      case 'MEDIUM': return AppColors.primary;
      case 'LARGE': return AppColors.warning;
      default: return AppColors.slate500;
    }
  }

  Future<void> _confirmCreation() async {
    if (_isSubmitting) return;

    if (_buildingName.trim().isEmpty) {
      setState(() => _showNameError = true);
      return;
    }
    setState(() {
      _showNameError = false;
      _isSubmitting = true;
    });

    try {
      final api = ApiClient();
      await api.createBuilding(
        type: widget.buildingType.toUpperCase(),
        tier: _selectedTier ?? 'SMALL',
        subType: _selectedSubType, // null for non-SHOP buildings (GARDEN, FARM, etc.)
        name: _buildingName.trim(),
      );
      
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('${buildingTypeName} başarıyla oluşturuldu!'),
            backgroundColor: Colors.green,
          ),
        );
        context.pop(true); // Return true to indicate success
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Hata: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _isSubmitting = false);
      }
    }
  }

  bool get _canProceed {
    if (_step == 1) return _selectedSubType != null;
    if (_step == 2) return _selectedTier != null;
    return true;
  }

  void _nextStep() {
    if (_canProceed) {
      setState(() => _step++);
    }
  }

  void _prevStep() {
    final minStep = isShop ? 1 : 2;
    if (_step > minStep) {
      setState(() => _step--);
    }
  }

  @override
  Widget build(BuildContext context) {
    final configsAsync = ref.watch(buildingConfigsProvider);
    final gameDataAsync = ref.watch(gameDataProvider);

    return Scaffold(
      backgroundColor: AppColors.slate900,
      appBar: AppBar(
        title: Text('Yeni $buildingTypeName Oluştur'),
        backgroundColor: AppColors.slate800,
        foregroundColor: Colors.white,
        leading: IconButton(
          icon: const Icon(Icons.close),
          onPressed: () => context.pop(),
        ),
      ),
      body: Column(
        children: [
          // Progress Bar
          _buildProgressBar(),
          
          // Content
          Expanded(
            child: configsAsync.when(
              data: (configs) => gameDataAsync.when(
                data: (gameData) => _buildContent(configs, gameData),
                loading: () => const Center(child: CircularProgressIndicator()),
                error: (e, _) => Center(child: Text('Hata: $e', style: const TextStyle(color: Colors.white))),
              ),
              loading: () => const Center(child: CircularProgressIndicator()),
              error: (e, _) => Center(child: Text('Hata: $e', style: const TextStyle(color: Colors.white))),
            ),
          ),
          
          // Footer Buttons
          _buildFooter(configsAsync.valueOrNull ?? {}),
        ],
      ),
    );
  }

  Widget _buildProgressBar() {
    final progress = displayStep / totalSteps;
    
    return Container(
      padding: const EdgeInsets.all(16),
      child: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                decoration: BoxDecoration(
                  color: AppColors.primary.withOpacity(0.2),
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Text(
                  'Adım $displayStep / $totalSteps',
                  style: TextStyle(
                    color: AppColors.primary,
                    fontWeight: FontWeight.bold,
                    fontSize: 12,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          ClipRRect(
            borderRadius: BorderRadius.circular(4),
            child: LinearProgressIndicator(
              value: progress,
              backgroundColor: AppColors.slate700,
              valueColor: AlwaysStoppedAnimation<Color>(AppColors.primary),
              minHeight: 8,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildContent(Map<String, dynamic> configs, List<Map<String, dynamic>> gameData) {
    switch (_step) {
      case 1:
        return _buildStep1SubType(gameData);
      case 2:
        return _buildStep2Tier(configs);
      case 3:
        return _buildStep3Confirmation(configs, gameData);
      default:
        return const SizedBox.shrink();
    }
  }

  Widget _buildStep1SubType(List<Map<String, dynamic>> gameData) {
    final options = _getSubTypeOptions(gameData);
    
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'İşletme Türünü Seçin',
            style: TextStyle(
              color: AppColors.slate300,
              fontSize: 16,
              fontWeight: FontWeight.w500,
            ),
          ),
          const SizedBox(height: 16),
          ...options.map((option) => _buildSubTypeOption(option)),
        ],
      ),
    );
  }

  Widget _buildSubTypeOption(Map<String, dynamic> option) {
    final id = option['id']?.toString() ?? '';
    final name = option['name']?.toString() ?? id;
    final description = option['description']?.toString() ?? '';
    final isSelected = _selectedSubType == id;

    return GestureDetector(
      onTap: () => setState(() => _selectedSubType = id),
      child: Container(
        margin: const EdgeInsets.only(bottom: 12),
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: isSelected ? AppColors.slate700 : AppColors.slate800,
          borderRadius: BorderRadius.circular(12),
          border: Border.all(
            color: isSelected ? AppColors.primary : AppColors.slate600,
            width: isSelected ? 2 : 1,
          ),
        ),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    name,
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  if (description.isNotEmpty) ...[
                    const SizedBox(height: 4),
                    Text(
                      description,
                      style: TextStyle(
                        color: AppColors.slate400,
                        fontSize: 14,
                      ),
                    ),
                  ],
                ],
              ),
            ),
            if (isSelected)
              Icon(Icons.check_circle, color: AppColors.primary, size: 24),
          ],
        ),
      ),
    );
  }

  Widget _buildStep2Tier(Map<String, dynamic> configs) {
    final tiers = ['SMALL', 'MEDIUM', 'LARGE'];
    
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Büyüklük Seçin',
            style: TextStyle(
              color: AppColors.slate300,
              fontSize: 16,
              fontWeight: FontWeight.w500,
            ),
          ),
          const SizedBox(height: 16),
          ...tiers.map((tier) => _buildTierOption(tier, configs)),
        ],
      ),
    );
  }

  Widget _buildTierOption(String tier, Map<String, dynamic> configs) {
    final config = _getConfig(configs, tier);
    final isSelected = _selectedTier == tier;
    final cost = config?['cost'] ?? 0;
    final maxSlots = config?['maxSlots'] ?? 0;
    final maxStock = config?['maxStock'] ?? 0;
    final productionRate = config?['productionRate'] ?? 0;
    final salesDuration = config?['salesDuration'] ?? 0;
    final productionDuration = config?['productionDuration'] ?? 0;

    return GestureDetector(
      onTap: () => setState(() => _selectedTier = tier),
      child: Container(
        margin: const EdgeInsets.only(bottom: 12),
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: isSelected ? AppColors.slate700 : AppColors.slate800,
          borderRadius: BorderRadius.circular(12),
          border: Border.all(
            color: isSelected ? AppColors.primary : AppColors.slate600,
            width: isSelected ? 2 : 1,
          ),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  _getTierLabel(tier),
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                  decoration: BoxDecoration(
                    color: _getTierBadgeColor(tier),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Text(
                    _getTierBadge(tier),
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 10,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            _buildTierDetail('Maliyet', cost, isCurrency: true),
            _buildTierDetail('Kapasite', '$maxSlots Slot'),
            if (isShop) ...[
              _buildTierDetail('Stok', maxStock),
              _buildTierDetail('Satış Süresi', '$salesDuration dakika'),
            ] else ...[
              _buildTierDetail('Üretim', '$productionRate adet'),
              _buildTierDetail('Üretim Süresi', '$productionDuration dakika'),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildTierDetail(String label, dynamic value, {bool isCurrency = false}) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            label,
            style: TextStyle(color: AppColors.slate400, fontSize: 14),
          ),
          if (isCurrency)
            Row(
              children: [
                const CurrencyIcon(size: 14),
                const SizedBox(width: 4),
                Text(
                  _formatNumber(value as int),
                  style: TextStyle(color: AppColors.success, fontWeight: FontWeight.bold, fontSize: 14),
                ),
              ],
            )
          else
            Text(
              value.toString(),
              style: const TextStyle(color: Colors.white, fontSize: 14),
            ),
        ],
      ),
    );
  }

  String _formatNumber(int number) {
    if (number >= 1000000) {
      return '${(number / 1000000).toStringAsFixed(1)}M';
    } else if (number >= 1000) {
      return '${(number / 1000).toStringAsFixed(1)}K';
    }
    return number.toString();
  }

  Widget _buildStep3Confirmation(Map<String, dynamic> configs, List<Map<String, dynamic>> gameData) {
    final config = _getConfig(configs, _selectedTier ?? 'SMALL');
    final cost = config?['cost'] ?? 0;
    final maxSlots = config?['maxSlots'] ?? 0;
    final maxStock = config?['maxStock'] ?? 0;
    final productionRate = config?['productionRate'] ?? 0;

    // Find subtype name
    String subTypeName = _selectedSubType ?? '';
    for (final item in gameData) {
      if (item['id']?.toString() == _selectedSubType) {
        subTypeName = item['name']?.toString() ?? _selectedSubType ?? '';
        break;
      }
    }

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Özet ve Onay',
            style: TextStyle(
              color: AppColors.slate300,
              fontSize: 16,
              fontWeight: FontWeight.w500,
            ),
          ),
          const SizedBox(height: 16),
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: AppColors.slate800,
              borderRadius: BorderRadius.circular(12),
              border: Border.all(color: AppColors.slate700),
            ),
            child: Column(
              children: [
                _buildSummaryRow('Tür', subTypeName),
                _buildSummaryRow('Büyüklük', _getTierLabel(_selectedTier ?? 'SMALL')),
                _buildSummaryRow('Toplam Maliyet', cost, isCurrency: true),
                _buildSummaryRow('Kapasite', '$maxSlots Slot'),
                _buildSummaryRow(
                  isShop ? 'Stok' : 'Üretim',
                  isShop ? maxStock.toString() : '$productionRate adet',
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),
          Text(
            'İşletme Adı *',
            style: TextStyle(
              color: AppColors.slate300,
              fontSize: 14,
              fontWeight: FontWeight.w500,
            ),
          ),
          const SizedBox(height: 8),
          TextField(
            onChanged: (value) {
              setState(() {
                _buildingName = value;
                if (value.isNotEmpty) _showNameError = false;
              });
            },
            maxLength: 50,
            style: const TextStyle(color: Colors.white),
            decoration: InputDecoration(
              hintText: 'Örn: Merkez Dükkanım, Ana Fabrika',
              hintStyle: TextStyle(color: AppColors.slate500),
              filled: true,
              fillColor: AppColors.slate800,
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: BorderSide(
                  color: _showNameError ? Colors.red : AppColors.slate600,
                ),
              ),
              enabledBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: BorderSide(
                  color: _showNameError ? Colors.red : AppColors.slate600,
                ),
              ),
              focusedBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: BorderSide(
                  color: _showNameError ? Colors.red : AppColors.primary,
                  width: 2,
                ),
              ),
              counterStyle: TextStyle(color: AppColors.slate500),
            ),
          ),
          if (_showNameError)
            Padding(
              padding: const EdgeInsets.only(top: 4),
              child: Text(
                'İşletme adı zorunludur.',
                style: TextStyle(color: Colors.red.shade400, fontSize: 12),
              ),
            ),
          const SizedBox(height: 16),
          Text(
            'Bu işletmeyi oluşturmak için onaylayın.',
            style: TextStyle(color: AppColors.slate400, fontSize: 14),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }

  Widget _buildSummaryRow(String label, dynamic value, {bool isCurrency = false}) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            label,
            style: TextStyle(color: AppColors.slate400, fontSize: 14),
          ),
          if (isCurrency)
            Row(
              children: [
                const CurrencyIcon(size: 16),
                const SizedBox(width: 4),
                Text(
                  _formatNumber(value as int),
                  style: TextStyle(
                    color: AppColors.success,
                    fontWeight: FontWeight.bold,
                    fontSize: 16,
                  ),
                ),
              ],
            )
          else
            Text(
              value.toString(),
              style: const TextStyle(
                color: Colors.white,
                fontWeight: FontWeight.w500,
                fontSize: 14,
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildFooter(Map<String, dynamic> configs) {
    final isFirstStep = isShop ? _step == 1 : _step == 2;
    final isLastStep = _step == 3;

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.slate800,
        border: Border(
          top: BorderSide(color: AppColors.slate700),
        ),
      ),
      child: Row(
        children: [
          if (!isFirstStep)
            Expanded(
              child: OutlinedButton(
                onPressed: _prevStep,
                style: OutlinedButton.styleFrom(
                  foregroundColor: AppColors.slate300,
                  side: BorderSide(color: AppColors.slate600),
                  padding: const EdgeInsets.symmetric(vertical: 16),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                ),
                child: const Text('Geri'),
              ),
            ),
          if (!isFirstStep) const SizedBox(width: 12),
          Expanded(
            flex: 2,
            child: ElevatedButton(
              onPressed: _isSubmitting 
                  ? null 
                  : (isLastStep ? _confirmCreation : (_canProceed ? _nextStep : null)),
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.primary,
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(vertical: 16),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
                disabledBackgroundColor: AppColors.slate600,
              ),
              child: _isSubmitting
                  ? const SizedBox(
                      height: 20,
                      width: 20,
                      child: CircularProgressIndicator(
                        strokeWidth: 2,
                        valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                      ),
                    )
                  : Text(
                      isLastStep ? 'Oluştur' : 'Devam',
                      style: const TextStyle(fontWeight: FontWeight.bold),
                    ),
            ),
          ),
        ],
      ),
    );
  }
}
