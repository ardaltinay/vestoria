import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/theme/app_theme.dart';
import '../../core/api/api_client.dart';
import '../../core/providers/auth_provider.dart';

// Announcements provider
final announcementsProvider = FutureProvider<List<Map<String, dynamic>>>((ref) async {
  final api = ApiClient();
  try {
    final response = await api.getAnnouncements();
    final List data = response.data ?? [];
    return data.map<Map<String, dynamic>>((item) => Map<String, dynamic>.from(item)).toList();
  } catch (e) {
    print('Announcements error: $e');
    return [];
  }
});

class SocialPage extends ConsumerStatefulWidget {
  const SocialPage({super.key});

  @override
  ConsumerState<SocialPage> createState() => _SocialPageState();
}

class _SocialPageState extends ConsumerState<SocialPage> {
  final TextEditingController _emailController = TextEditingController();

  @override
  void dispose() {
    _emailController.dispose();
    super.dispose();
  }

  bool get _isValidEmail {
    final email = _emailController.text;
    return RegExp(r'^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$').hasMatch(email);
  }

  String _getTypeLabel(String type) {
    switch (type.toUpperCase()) {
      case 'UPDATE':
        return 'Güncelleme';
      case 'NEWS':
        return 'Haber';
      case 'EVENT':
        return 'Etkinlik';
      default:
        return type;
    }
  }

  Color _getTypeColor(String type) {
    switch (type.toUpperCase()) {
      case 'UPDATE':
        return const Color(0xFF3b82f6); // blue
      case 'NEWS':
        return const Color(0xFF10b981); // emerald
      case 'EVENT':
        return const Color(0xFFf59e0b); // amber
      default:
        return AppColors.slate500;
    }
  }

  String _formatDate(String? dateString) {
    if (dateString == null) return '';
    try {
      final date = DateTime.parse(dateString);
      final now = DateTime.now();
      final diff = now.difference(date);
      
      if (diff.inDays == 0) {
        if (diff.inHours == 0) {
          return '${diff.inMinutes} dakika önce';
        }
        return '${diff.inHours} saat önce';
      } else if (diff.inDays == 1) {
        return 'Dün';
      } else if (diff.inDays < 7) {
        return '${diff.inDays} gün önce';
      } else {
        final months = [
          '', 'Oca', 'Şub', 'Mar', 'Nis', 'May', 'Haz',
          'Tem', 'Ağu', 'Eyl', 'Eki', 'Kas', 'Ara'
        ];
        return '${date.day} ${months[date.month]}';
      }
    } catch (e) {
      return '';
    }
  }

  void _handleInvite() {
    if (!_isValidEmail) return;
    
    // Open email client with invite link
    final email = _emailController.text;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Davet linki $email adresine gönderilecek'),
        backgroundColor: Colors.green,
      ),
    );
    _emailController.clear();
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    final announcementsAsync = ref.watch(announcementsProvider);
    final user = ref.watch(authProvider).user;
    final isAdmin = user?.role?.toUpperCase() == 'ADMIN';

    return RefreshIndicator(
      onRefresh: () async => ref.invalidate(announcementsProvider),
      child: SingleChildScrollView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Header
            Row(
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Sosyal',
                        style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      Text(
                        'Arkadaşlarını davet et ve gelişmelerden haberdar ol',
                        style: TextStyle(
                          color: AppColors.slate500,
                          fontSize: 14,
                        ),
                      ),
                    ],
                  ),
                ),
                if (isAdmin)
                  ElevatedButton.icon(
                    onPressed: () {
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('Duyuru ekleme özelliği yakında')),
                      );
                    },
                    icon: const Icon(Icons.add, size: 18),
                    label: const Text('Duyuru Ekle'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.primary,
                    ),
                  ),
              ],
            ),
            
            const SizedBox(height: 24),

            // Invite Card
            Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: AppColors.surface,
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: AppColors.slate200),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Container(
                        width: 40,
                        height: 40,
                        decoration: BoxDecoration(
                          color: AppColors.primary.withOpacity(0.1),
                          shape: BoxShape.circle,
                        ),
                        child: Icon(
                          Icons.person_add,
                          color: AppColors.primary,
                          size: 20,
                        ),
                      ),
                      const SizedBox(width: 12),
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Arkadaşını Davet Et',
                            style: Theme.of(context).textTheme.titleMedium?.copyWith(
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          Text(
                            'Birlikte büyüyün',
                            style: TextStyle(
                              color: AppColors.slate500,
                              fontSize: 12,
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                  const SizedBox(height: 20),
                  
                  // Email Input
                  TextField(
                    controller: _emailController,
                    keyboardType: TextInputType.emailAddress,
                    onChanged: (_) => setState(() {}),
                    decoration: InputDecoration(
                      hintText: 'ornek@email.com',
                      labelText: 'E-posta Adresi',
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                    ),
                  ),
                  const SizedBox(height: 16),
                  
                  // Send Button
                  SizedBox(
                    width: double.infinity,
                    child: ElevatedButton.icon(
                      onPressed: _isValidEmail ? _handleInvite : null,
                      icon: const Icon(Icons.email),
                      label: const Text('Davet Gönder'),
                      style: ElevatedButton.styleFrom(
                        padding: const EdgeInsets.symmetric(vertical: 14),
                        backgroundColor: AppColors.primary,
                        disabledBackgroundColor: AppColors.slate300,
                      ),
                    ),
                  ),
                  const SizedBox(height: 12),
                  
                  Text(
                    'Davet linki e-posta uygulamanız üzerinden gönderilecektir.',
                    textAlign: TextAlign.center,
                    style: TextStyle(
                      color: AppColors.slate400,
                      fontSize: 12,
                    ),
                  ),
                ],
              ),
            ),
            
            const SizedBox(height: 24),

            // Newspaper Section
            Container(
              decoration: BoxDecoration(
                color: AppColors.surface,
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: AppColors.slate200),
              ),
              child: Column(
                children: [
                  // Newspaper Header
                  Container(
                    padding: const EdgeInsets.all(16),
                    decoration: BoxDecoration(
                      color: AppColors.slate50,
                      borderRadius: const BorderRadius.vertical(top: Radius.circular(16)),
                      border: Border(bottom: BorderSide(color: AppColors.slate200)),
                    ),
                    child: Row(
                      children: [
                        Container(
                          width: 40,
                          height: 40,
                          decoration: BoxDecoration(
                            color: AppColors.warning.withOpacity(0.1),
                            shape: BoxShape.circle,
                          ),
                          child: Icon(
                            Icons.newspaper,
                            color: AppColors.warning,
                            size: 20,
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                'Vestoria Gazetesi',
                                style: Theme.of(context).textTheme.titleMedium?.copyWith(
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                              Text(
                                'Son gelişmeler ve duyurular',
                                style: TextStyle(
                                  color: AppColors.slate500,
                                  fontSize: 12,
                                ),
                              ),
                            ],
                          ),
                        ),
                        announcementsAsync.when(
                          data: (announcements) => Text(
                            'SAYI #${announcements.length + 100}',
                            style: TextStyle(
                              color: AppColors.slate400,
                              fontSize: 10,
                              fontFamily: 'monospace',
                            ),
                          ),
                          loading: () => const SizedBox(),
                          error: (_, __) => const SizedBox(),
                        ),
                      ],
                    ),
                  ),
                  
                  // Announcements List
                  announcementsAsync.when(
                    data: (announcements) {
                      if (announcements.isEmpty) {
                        return Padding(
                          padding: const EdgeInsets.all(48),
                          child: Column(
                            children: [
                              Container(
                                width: 64,
                                height: 64,
                                decoration: BoxDecoration(
                                  color: AppColors.slate100,
                                  shape: BoxShape.circle,
                                ),
                                child: Icon(
                                  Icons.newspaper,
                                  size: 32,
                                  color: AppColors.slate400,
                                ),
                              ),
                              const SizedBox(height: 16),
                              Text(
                                'Henüz haber yok',
                                style: Theme.of(context).textTheme.titleMedium?.copyWith(
                                  fontWeight: FontWeight.w500,
                                ),
                              ),
                              const SizedBox(height: 4),
                              Text(
                                'Şimdilik her şey sakin görünüyor.',
                                style: TextStyle(color: AppColors.slate500),
                              ),
                            ],
                          ),
                        );
                      }
                      
                      return ListView.separated(
                        shrinkWrap: true,
                        physics: const NeverScrollableScrollPhysics(),
                        itemCount: announcements.length,
                        separatorBuilder: (_, __) => Divider(
                          height: 1,
                          color: AppColors.slate100,
                        ),
                        itemBuilder: (context, index) {
                          final news = announcements[index];
                          return _buildAnnouncementItem(news);
                        },
                      );
                    },
                    loading: () => Padding(
                      padding: const EdgeInsets.all(32),
                      child: Center(child: CircularProgressIndicator()),
                    ),
                    error: (error, stack) => Padding(
                      padding: const EdgeInsets.all(32),
                      child: Center(
                        child: Text(
                          'Yüklenirken hata oluştu',
                          style: TextStyle(color: AppColors.error),
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildAnnouncementItem(Map<String, dynamic> news) {
    final type = news['type'] ?? 'NEWS';
    final title = news['title'] ?? '';
    final content = news['content'] ?? '';
    final createdTime = news['createdTime'];
    final color = _getTypeColor(type);

    return Padding(
      padding: const EdgeInsets.all(16),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Type Badge
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
            decoration: BoxDecoration(
              color: color.withOpacity(0.1),
              borderRadius: BorderRadius.circular(6),
              border: Border.all(color: color.withOpacity(0.2)),
            ),
            child: Text(
              _getTypeLabel(type),
              style: TextStyle(
                color: color,
                fontSize: 10,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
          const SizedBox(width: 12),
          
          // Content
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Expanded(
                      child: Text(
                        title,
                        style: Theme.of(context).textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ),
                    Text(
                      _formatDate(createdTime),
                      style: TextStyle(
                        color: AppColors.slate400,
                        fontSize: 12,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 4),
                Text(
                  content,
                  style: TextStyle(
                    color: AppColors.slate600,
                    fontSize: 13,
                    height: 1.5,
                  ),
                  maxLines: 3,
                  overflow: TextOverflow.ellipsis,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
