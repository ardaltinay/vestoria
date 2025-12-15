import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../core/theme/app_theme.dart';
import '../../core/providers/auth_provider.dart';
import '../../core/providers/notifications_provider.dart';
import '../../core/widgets/currency_icon.dart';
import '../../core/widgets/vestoria_logo.dart';
import '../../core/widgets/building_icons.dart';
import '../../core/api/api_client.dart';
import 'package:intl/intl.dart';

class HomePage extends ConsumerStatefulWidget {
  final Widget child;
  
  const HomePage({super.key, required this.child});

  @override
  ConsumerState<HomePage> createState() => _HomePageState();
}

class _HomePageState extends ConsumerState<HomePage> {
  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey<ScaffoldState>();
  
  int _currentIndex = 0;
  
  final List<_NavItem> _navItems = [
    _NavItem(path: '/home', label: 'Ana Sayfa', icon: Icons.home_outlined, activeIcon: Icons.home),
    _NavItem(path: '/home/shops', label: 'Dükkanlar', icon: Icons.storefront_outlined, activeIcon: Icons.storefront),
    _NavItem(path: '/home/market', label: 'Pazar', icon: Icons.shopping_cart_outlined, activeIcon: Icons.shopping_cart),
    _NavItem(path: '/home/inventory', label: 'Envanter', icon: Icons.inventory_2_outlined, activeIcon: Icons.inventory_2),
    _NavItem(path: '/home/profile', label: 'Profil', icon: Icons.person_outline, activeIcon: Icons.person),
  ];

  final List<_SidebarSection> _sidebarSections = [
    _SidebarSection(
      title: 'İşletmeler',
      items: [
        _SidebarItem(path: '/home/shops', label: 'Dükkanlar', icon: BuildingIcons.shopOutlined),
        _SidebarItem(path: '/home/gardens', label: 'Bahçeler', icon: BuildingIcons.gardenOutlined),
        _SidebarItem(path: '/home/farms', label: 'Çiftlikler', icon: BuildingIcons.farmOutlined),
        _SidebarItem(path: '/home/factories', label: 'Fabrikalar', icon: BuildingIcons.factoryOutlined),
        _SidebarItem(path: '/home/mines', label: 'Madenler', icon: BuildingIcons.mineOutlined),
      ],
    ),
    _SidebarSection(
      title: 'Ticaret',
      items: [
        _SidebarItem(path: '/home/market', label: 'Pazar', icon: Icons.shopping_cart_outlined),
        _SidebarItem(path: '/home/inventory', label: 'Envanter', icon: Icons.inventory_2_outlined),
      ],
    ),
    _SidebarSection(
      title: 'Sosyal',
      items: [
        _SidebarItem(path: '/home/social', label: 'Sosyal', icon: Icons.newspaper_outlined),
        _SidebarItem(path: '/home/profile', label: 'Profil', icon: Icons.person_outline),
      ],
    ),
  ];

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    _updateCurrentIndex();
  }

  void _updateCurrentIndex() {
    final location = GoRouterState.of(context).matchedLocation;
    for (int i = 0; i < _navItems.length; i++) {
      if (location == _navItems[i].path || location.startsWith('${_navItems[i].path}/')) {
        if (_currentIndex != i) {
          setState(() => _currentIndex = i);
        }
        return;
      }
    }
  }

  String formatCurrency(double amount) {
    final intValue = amount.toInt();
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

  void _showNotificationsSheet(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) => DraggableScrollableSheet(
        initialChildSize: 0.6,
        minChildSize: 0.3,
        maxChildSize: 0.9,
        builder: (context, scrollController) => Container(
          decoration: BoxDecoration(
            color: AppColors.surface,
            borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
          ),
          child: Column(
            children: [
              // Handle
              Container(
                margin: EdgeInsets.only(top: 12),
                width: 40,
                height: 4,
                decoration: BoxDecoration(
                  color: AppColors.slate300,
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
              // Header
              Padding(
                padding: EdgeInsets.all(16),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      'Bildirimler',
                      style: Theme.of(context).textTheme.titleLarge?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    TextButton(
                      onPressed: () async {
                        try {
                          final api = ApiClient();
                          await api.markAllNotificationsRead();
                          ref.invalidate(notificationsProvider);
                        } catch (e) {
                          // Ignore
                        }
                      },
                      child: Text('Tümünü Okundu Yap'),
                    ),
                  ],
                ),
              ),
              Divider(height: 1),
              // List
              Expanded(
                child: Consumer(
                  builder: (context, ref, _) {
                    final notificationsAsync = ref.watch(notificationsProvider);
                    return notificationsAsync.when(
                      data: (notifications) {
                        if (notifications.isEmpty) {
                          return Center(
                            child: Column(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                Icon(Icons.notifications_off, size: 48, color: AppColors.slate300),
                                SizedBox(height: 12),
                                Text('Henüz bildiriminiz yok', style: TextStyle(color: AppColors.slate500)),
                              ],
                            ),
                          );
                        }
                        return ListView.separated(
                          controller: scrollController,
                          itemCount: notifications.length,
                          separatorBuilder: (_, __) => Divider(height: 1),
                          itemBuilder: (context, index) {
                            final notif = notifications[index];
                            return ListTile(
                              leading: Container(
                                width: 8,
                                height: 8,
                                decoration: BoxDecoration(
                                  color: notif.isRead ? AppColors.slate200 : AppColors.primary,
                                  shape: BoxShape.circle,
                                ),
                              ),
                              title: Text(
                                notif.message,
                                style: TextStyle(
                                  fontWeight: notif.isRead ? FontWeight.normal : FontWeight.w600,
                                ),
                              ),
                              subtitle: Text(
                                _formatRelativeTime(notif.createdAt),
                                style: TextStyle(color: AppColors.slate400, fontSize: 12),
                              ),
                            );
                          },
                        );
                      },
                      loading: () => Center(child: CircularProgressIndicator()),
                      error: (e, s) => Center(child: Text('Hata: $e')),
                    );
                  },
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  String _formatRelativeTime(DateTime dateTime) {
    final now = DateTime.now();
    final diff = now.difference(dateTime);
    if (diff.inMinutes < 1) return 'Az önce';
    if (diff.inMinutes < 60) return '${diff.inMinutes} dk önce';
    if (diff.inHours < 24) return '${diff.inHours} saat önce';
    if (diff.inDays < 7) return '${diff.inDays} gün önce';
    return DateFormat('d MMM', 'tr_TR').format(dateTime);
  }

  @override
  Widget build(BuildContext context) {
    final authState = ref.watch(authProvider);
    final user = authState.user;
    final isDesktop = MediaQuery.of(context).size.width >= 1024;
    
    return Scaffold(
      key: _scaffoldKey,
      backgroundColor: AppColors.background,
      drawer: isDesktop ? null : _buildDrawer(context, user),
      body: SafeArea(
        child: Row(
          children: [
            // Desktop Sidebar
            if (isDesktop)
              SizedBox(
                width: 280,
                child: _buildSidebar(context, user),
              ),
            
            // Main Content
            Expanded(
              child: Column(
                children: [
                  // Top Header
                  _buildHeader(context, user, isDesktop),
                  
                  // Page Content
                  Expanded(
                    child: widget.child,
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
      bottomNavigationBar: isDesktop ? null : _buildBottomNav(context),
    );
  }

  Widget _buildHeader(BuildContext context, user, bool isDesktop) {
    final location = GoRouterState.of(context).matchedLocation;
    String title = 'Ana Sayfa';
    
    for (var section in _sidebarSections) {
      for (var item in section.items) {
        if (location.startsWith(item.path)) {
          title = item.label;
          break;
        }
      }
    }
    
    return Container(
      height: 64,
      decoration: BoxDecoration(
        color: AppColors.surface,
        border: Border(
          bottom: BorderSide(color: AppColors.slate200),
        ),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Row(
        children: [
          if (!isDesktop)
            IconButton(
              icon: const Icon(Icons.menu),
              onPressed: () => _scaffoldKey.currentState?.openDrawer(),
            ),
          
          const SizedBox(width: 8),
          
          // Breadcrumb
          Text(
            title,
            style: Theme.of(context).textTheme.titleLarge,
          ),
          
          const Spacer(),
          
          // Notifications Bell
          Stack(
            children: [
              IconButton(
                icon: const Icon(Icons.notifications_outlined),
                onPressed: () => _showNotificationsSheet(context),
              ),
              Positioned(
                right: 8,
                top: 8,
                child: Consumer(
                  builder: (context, ref, _) {
                    final unreadCount = ref.watch(unreadNotificationsCountProvider);
                    if (unreadCount == 0) return const SizedBox.shrink();
                    return Container(
                      width: 16,
                      height: 16,
                      decoration: BoxDecoration(
                        color: AppColors.error,
                        shape: BoxShape.circle,
                      ),
                      child: Center(
                        child: Text(
                          unreadCount > 9 ? '9+' : '$unreadCount',
                          style: const TextStyle(
                            color: Colors.white,
                            fontSize: 10,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                    );
                  },
                ),
              ),
            ],
          ),
          
          const SizedBox(width: 8),
          
          // Balance
          if (user != null)
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
              decoration: BoxDecoration(
                color: AppColors.success.withOpacity(0.1),
                borderRadius: BorderRadius.circular(20),
              ),
              child: Row(
                children: [
                  CurrencyIcon(
                    size: 18, variant: CurrencyIconVariant.success),
                  const SizedBox(width: 6),
                  Text(
                    formatCurrency(user.balance),
                    style: Theme.of(context).textTheme.titleSmall?.copyWith(
                      color: AppColors.success,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildSidebar(BuildContext context, user) {
    return Container(
      color: AppColors.surface,
      child: Column(
        children: [
          // Logo
          // Logo with SafeArea
          SafeArea(
            bottom: false,
            child: Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                border: Border(bottom: BorderSide(color: AppColors.slate100)),
              ),
              child: Row(
                children: [
                  const VestoriaLogo(size: 36),
                  const SizedBox(width: 12),
                  Text(
                    'Vestoria',
                    style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
            ),
          ),
          
          // User Info
          if (user != null) _buildUserInfo(context, user),
          
          // Navigation
          Expanded(
            child: ListView(
              padding: const EdgeInsets.all(12),
              children: [
                for (var section in _sidebarSections) ...[
                  Padding(
                    padding: const EdgeInsets.only(left: 12, top: 16, bottom: 8),
                    child: Text(
                      section.title.toUpperCase(),
                      style: Theme.of(context).textTheme.labelSmall?.copyWith(
                        color: AppColors.slate400,
                        letterSpacing: 1,
                      ),
                    ),
                  ),
                  for (var item in section.items)
                    _buildSidebarNavItem(context, item),
                ],
              ],
            ),
          ),
          
          // Footer
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              border: Border(top: BorderSide(color: AppColors.slate100)),
            ),
            child: Text(
              'Vestoria © 2025',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                color: AppColors.slate400,
              ),
              textAlign: TextAlign.center,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildUserInfo(BuildContext context, user) {
    return Container(
      margin: const EdgeInsets.all(12),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: AppColors.slate50,
        borderRadius: BorderRadius.circular(12),
      ),
      child: Column(
        children: [
          Row(
            children: [
              CircleAvatar(
                radius: 20,
                backgroundColor: AppColors.primary,
                child: Text(
                  user.username[0].toUpperCase(),
                  style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      user.username,
                      style: Theme.of(context).textTheme.titleSmall,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                    const SizedBox(height: 4),
                    Row(
                      children: [
                        Text(
                          'Seviye ${user.level}',
                          style: Theme.of(context).textTheme.bodySmall,
                        ),
                        const Spacer(),
                        Text(
                          '${user.xp}/${user.xpForNextLevel} XP',
                          style: Theme.of(context).textTheme.bodySmall,
                        ),
                      ],
                    ),
                    const SizedBox(height: 4),
                    LinearProgressIndicator(
                      value: user.xpProgress,
                      backgroundColor: AppColors.slate200,
                      valueColor: AlwaysStoppedAnimation(AppColors.primary),
                      minHeight: 4,
                      borderRadius: BorderRadius.circular(2),
                    ),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              Expanded(
                child: _buildStatCard(
                  context, 
                  'Nakit', 
                  formatCurrency(user.balance),
                  AppColors.success,
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: _buildStatCard(
                  context, 
                  'Prestij', 
                  '★ 0',
                  AppColors.warning,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildStatCard(BuildContext context, String label, String value, Color color) {
    return Container(
      padding: const EdgeInsets.all(8),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: AppColors.slate200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            label.toUpperCase(),
            style: Theme.of(context).textTheme.labelSmall?.copyWith(
              color: AppColors.slate400,
            ),
          ),
          const SizedBox(height: 2),
          Text(
            value,
            style: Theme.of(context).textTheme.titleSmall?.copyWith(
              color: color,
              fontWeight: FontWeight.bold,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSidebarNavItem(BuildContext context, _SidebarItem item) {
    final location = GoRouterState.of(context).matchedLocation;
    final isActive = location.startsWith(item.path);
    
    return Padding(
      padding: const EdgeInsets.only(bottom: 4),
      child: Material(
        color: isActive ? AppColors.primary.withOpacity(0.1) : Colors.transparent,
        borderRadius: BorderRadius.circular(8),
        child: InkWell(
          borderRadius: BorderRadius.circular(8),
          onTap: () => context.go(item.path),
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
            child: Row(
              children: [
                Icon(
                  item.icon,
                  size: 20,
                  color: isActive ? AppColors.primary : AppColors.slate400,
                ),
                const SizedBox(width: 12),
                Text(
                  item.label,
                  style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    color: isActive ? AppColors.primary : AppColors.slate600,
                    fontWeight: isActive ? FontWeight.w600 : FontWeight.normal,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildDrawer(BuildContext context, user) {
    return Drawer(
      child: _buildSidebar(context, user),
    );
  }

  Widget _buildBottomNav(BuildContext context) {
    // Compute active index from router location instead of stored state
    final location = GoRouterState.of(context).matchedLocation;
    int currentIndex = 0;
    
    // Check in reverse order (most specific paths first) to avoid /home matching everything
    for (int i = _navItems.length - 1; i >= 0; i--) {
      final path = _navItems[i].path;
      // Exact match or startsWith path + / (to match child routes)
      if (location == path || location.startsWith('$path/')) {
        currentIndex = i;
        break;
      }
    }
    
    // Special case: if location is exactly /home, index should be 0
    if (location == '/home') {
      currentIndex = 0;
    }
    
    return Container(
      decoration: BoxDecoration(
        color: AppColors.surface,
        border: Border(top: BorderSide(color: AppColors.slate200)),
      ),
      child: SafeArea(
        child: SizedBox(
          height: 64,
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: _navItems.asMap().entries.map((entry) {
              final index = entry.key;
              final item = entry.value;
              final isActive = currentIndex == index;
              
              return Expanded(
                child: InkWell(
                  onTap: () {
                    context.go(item.path);
                  },
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(
                        isActive ? item.activeIcon : item.icon,
                        color: isActive ? AppColors.primary : AppColors.slate400,
                        size: 24,
                      ),
                      const SizedBox(height: 4),
                      Text(
                        item.label,
                        style: TextStyle(
                          fontSize: 10,
                          fontWeight: isActive ? FontWeight.w600 : FontWeight.w500,
                          color: isActive ? AppColors.primary : AppColors.slate500,
                        ),
                      ),
                    ],
                  ),
                ),
              );
            }).toList(),
          ),
        ),
      ),
    );
  }
}

class _NavItem {
  final String path;
  final String label;
  final IconData icon;
  final IconData activeIcon;

  _NavItem({
    required this.path,
    required this.label,
    required this.icon,
    required this.activeIcon,
  });
}

class _SidebarSection {
  final String title;
  final List<_SidebarItem> items;

  _SidebarSection({required this.title, required this.items});
}

class _SidebarItem {
  final String path;
  final String label;
  final IconData icon;

  _SidebarItem({required this.path, required this.label, required this.icon});
}
