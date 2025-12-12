import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/theme/app_theme.dart';
import '../../core/providers/auth_provider.dart';
import '../../core/widgets/currency_icon.dart';
import '../../core/api/api_client.dart';
import '../../data/models/building.dart';

// Provider for all buildings count
final allBuildingsProvider = FutureProvider<int>((ref) async {
  final api = ApiClient();
  try {
    final response = await api.getAllBuildings();
    final List buildings = response.data ?? [];
    return buildings.length;
  } catch (e) {
    return 0;
  }
});

class ProfilePage extends ConsumerWidget {
  const ProfilePage({super.key});

  String _getAvatarUrl(String? username, int size) {
    final name = username ?? 'user';
    return 'https://api.dicebear.com/7.x/initials/png?seed=$name&size=$size';
  }

  String _formatDate(String? dateString) {
    if (dateString == null) return '-';
    try {
      final date = DateTime.parse(dateString);
      final months = [
        '', 'Ocak', 'Şubat', 'Mart', 'Nisan', 'Mayıs', 'Haziran',
        'Temmuz', 'Ağustos', 'Eylül', 'Ekim', 'Kasım', 'Aralık'
      ];
      return '${date.day} ${months[date.month]} ${date.year}';
    } catch (e) {
      return '-';
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

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final authState = ref.watch(authProvider);
    final user = authState.user;
    final buildingsAsync = ref.watch(allBuildingsProvider);
    
    final level = user?.level ?? 1;
    final xp = user?.xp ?? 0;
    final nextLevelXp = level * 1000;
    final xpPercentage = ((xp / nextLevelXp) * 100).clamp(0.0, 100.0);

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Profile Header Card
          Container(
            padding: const EdgeInsets.all(24),
            decoration: BoxDecoration(
              color: AppColors.surface,
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: AppColors.slate200),
            ),
            child: Column(
              children: [
                // Avatar and Info Row
                Row(
                  children: [
                    // Avatar
                    Container(
                      width: 96,
                      height: 96,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        border: Border.all(color: Colors.white, width: 4),
                        boxShadow: [
                          BoxShadow(
                            color: Colors.black.withOpacity(0.1),
                            blurRadius: 16,
                          ),
                        ],
                      ),
                      child: ClipOval(
                        child: Image.network(
                          _getAvatarUrl(user?.username, 256),
                          fit: BoxFit.cover,
                          errorBuilder: (_, __, ___) => Container(
                            color: AppColors.primary.withOpacity(0.1),
                            child: Icon(
                              Icons.person,
                              size: 48,
                              color: AppColors.primary,
                            ),
                          ),
                        ),
                      ),
                    ),
                    const SizedBox(width: 20),
                    
                    // User Info
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            user?.username ?? 'Kullanıcı',
                            style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          const SizedBox(height: 8),
                          
                          // Badges Row
                          Wrap(
                            spacing: 8,
                            runSpacing: 8,
                            children: [
                              // Level Badge
                              Container(
                                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                                decoration: BoxDecoration(
                                  color: AppColors.primary.withOpacity(0.1),
                                  borderRadius: BorderRadius.circular(20),
                                  border: Border.all(color: AppColors.primary.withOpacity(0.2)),
                                ),
                                child: Text(
                                  'Seviye $level',
                                  style: TextStyle(
                                    color: AppColors.primary,
                                    fontWeight: FontWeight.bold,
                                    fontSize: 12,
                                  ),
                                ),
                              ),
                              
                              // Balance Badge
                              Container(
                                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                                decoration: BoxDecoration(
                                  color: AppColors.success.withOpacity(0.1),
                                  borderRadius: BorderRadius.circular(20),
                                  border: Border.all(color: AppColors.success.withOpacity(0.2)),
                                ),
                                child: Row(
                                  mainAxisSize: MainAxisSize.min,
                                  children: [
                                    CurrencyIcon(
                                      size: 14,
                                      variant: CurrencyIconVariant.success,
                                    ),
                                    const SizedBox(width: 4),
                                    Text(
                                      _formatCurrency(user?.balance ?? 0),
                                      style: TextStyle(
                                        color: AppColors.success,
                                        fontWeight: FontWeight.bold,
                                        fontSize: 12,
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
                
                const SizedBox(height: 24),
                
                // XP Progress Bar
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          'XP İlerlemesi',
                          style: TextStyle(
                            color: AppColors.slate500,
                            fontWeight: FontWeight.bold,
                            fontSize: 12,
                          ),
                        ),
                        Text(
                          '$xp / $nextLevelXp XP',
                          style: TextStyle(
                            color: AppColors.slate500,
                            fontWeight: FontWeight.bold,
                            fontSize: 12,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    Container(
                      height: 12,
                      decoration: BoxDecoration(
                        color: AppColors.slate100,
                        borderRadius: BorderRadius.circular(6),
                        border: Border.all(color: AppColors.slate200),
                      ),
                      child: ClipRRect(
                        borderRadius: BorderRadius.circular(6),
                        child: FractionallySizedBox(
                          alignment: Alignment.centerLeft,
                          widthFactor: xpPercentage / 100,
                          child: Container(
                            decoration: BoxDecoration(
                              gradient: LinearGradient(
                                colors: [AppColors.primary, AppColors.primary.withOpacity(0.8)],
                              ),
                            ),
                          ),
                        ),
                      ),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      'Bir sonraki seviyeye ulaşmak için ${nextLevelXp - xp} XP daha kazanmalısın.',
                      style: TextStyle(
                        color: AppColors.slate400,
                        fontSize: 12,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
          
          const SizedBox(height: 16),
          
          // Stats Grid
          GridView.count(
            crossAxisCount: 2,
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            crossAxisSpacing: 12,
            mainAxisSpacing: 12,
            childAspectRatio: 1.5,
            children: [
              // Total Worth
              _buildStatCard(
                context,
                title: 'Toplam Varlık',
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    CurrencyIcon(size: 24, variant: CurrencyIconVariant.success),
                    const SizedBox(width: 8),
                    Text(
                      _formatCurrency(user?.balance ?? 0),
                      style: Theme.of(context).textTheme.titleLarge?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ],
                ),
              ),
              
              // Building Count
              _buildStatCard(
                context,
                title: 'İşletme Sayısı',
                child: buildingsAsync.when(
                  data: (count) => Text(
                    count.toString(),
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  loading: () => const SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  ),
                  error: (_, __) => Text(
                    '0',
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ),
              
              // Registration Date
              _buildStatCard(
                context,
                title: 'Kayıt Tarihi',
                child: Text(
                  _formatDate(user?.createdTime),
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
              
              // Prestige Score
              _buildStatCard(
                context,
                title: 'Prestij Puanı',
                child: Text(
                  '0',
                  style: Theme.of(context).textTheme.titleLarge?.copyWith(
                    fontWeight: FontWeight.bold,
                    color: AppColors.warning,
                  ),
                ),
              ),
            ],
          ),
          
          const SizedBox(height: 24),
          
          // Logout Section
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: AppColors.surface,
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: AppColors.slate200),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Hesap',
                  style: TextStyle(
                    color: AppColors.slate500,
                    fontWeight: FontWeight.w600,
                    fontSize: 14,
                  ),
                ),
                const SizedBox(height: 12),
                SizedBox(
                  width: double.infinity,
                  child: OutlinedButton.icon(
                    onPressed: () => _showLogoutDialog(context, ref),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: AppColors.error,
                      side: BorderSide(color: AppColors.error.withOpacity(0.3)),
                      padding: const EdgeInsets.symmetric(vertical: 14),
                    ),
                    icon: const Icon(Icons.logout),
                    label: const Text('Çıkış Yap'),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
  
  void _showLogoutDialog(BuildContext context, WidgetRef ref) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Çıkış Yap'),
        content: const Text('Hesabınızdan çıkış yapmak istediğinize emin misiniz?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('İptal'),
          ),
          ElevatedButton(
            onPressed: () async {
              Navigator.pop(context);
              await ref.read(authProvider.notifier).logout();
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.error,
              foregroundColor: Colors.white,
            ),
            child: const Text('Çıkış Yap'),
          ),
        ],
      ),
    );
  }

  Widget _buildStatCard(BuildContext context, {required String title, required Widget child}) {
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
          Text(
            title,
            style: TextStyle(
              color: AppColors.slate500,
              fontWeight: FontWeight.w500,
              fontSize: 13,
            ),
          ),
          const SizedBox(height: 8),
          child,
        ],
      ),
    );
  }
}
