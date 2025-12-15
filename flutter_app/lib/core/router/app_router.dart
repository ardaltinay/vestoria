import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../providers/auth_provider.dart';
import '../../features/auth/login_page.dart';
import '../../features/auth/register_page.dart';
import '../../features/home/home_page.dart';
import '../../features/landing/landing_page.dart';
import '../../features/dashboard/dashboard_page.dart';
import '../../features/shops/shops_page.dart';
import '../../features/buildings/building_detail_page.dart';
import '../../features/buildings/buildings_pages.dart';
import '../../features/buildings/create_building_wizard.dart';
import '../../features/marketplace/marketplace_page.dart';
import '../../features/inventory/inventory_page.dart';
import '../../features/social/social_page.dart';
import '../../features/profile/profile_page.dart';
import '../../features/reports/reports_page.dart';

// Auth state notifier for GoRouter refresh
class AuthRefreshNotifier extends ChangeNotifier {
  AuthRefreshNotifier(this._ref) {
    _ref.listen(authProvider, (_, __) {
      notifyListeners();
    });
  }
  
  final Ref _ref;
}

final _authRefreshProvider = Provider<AuthRefreshNotifier>((ref) {
  return AuthRefreshNotifier(ref);
});

final routerProvider = Provider<GoRouter>((ref) {
  final authRefreshNotifier = ref.watch(_authRefreshProvider);
  
  return GoRouter(
    initialLocation: '/',
    refreshListenable: authRefreshNotifier,
    redirect: (context, state) {
      final authState = ref.read(authProvider);
      final isAuthenticated = authState.isAuthenticated;
      final isLoading = authState.isLoading;
      final isAuthRoute = state.matchedLocation == '/login' || 
                          state.matchedLocation == '/register' ||
                          state.matchedLocation == '/';
      
      // Don't redirect while loading
      if (isLoading) return null;
      
      // If not authenticated and trying to access protected route
      if (!isAuthenticated && !isAuthRoute) {
        return '/login';
      }
      
      // If authenticated and trying to access auth route (except landing)
      if (isAuthenticated && (state.matchedLocation == '/login' || 
                               state.matchedLocation == '/register')) {
        return '/home';
      }
      
      return null;
    },
    routes: [
      // Landing
      GoRoute(
        path: '/',
        builder: (context, state) => const LandingPage(),
      ),
      
      // Auth Routes
      GoRoute(
        path: '/login',
        builder: (context, state) => const LoginPage(),
      ),
      GoRoute(
        path: '/register',
        builder: (context, state) => const RegisterPage(),
      ),
      
      // Home (Main Shell)
      ShellRoute(
        builder: (context, state, child) => HomePage(child: child),
        routes: [
          GoRoute(
            path: '/home',
            builder: (context, state) => const DashboardPage(),
          ),
          GoRoute(
            path: '/home/shops',
            builder: (context, state) => const ShopsPage(),
          ),
          GoRoute(
            path: '/home/farms',
            builder: (context, state) => const FarmsPage(),
          ),
          GoRoute(
            path: '/home/factories',
            builder: (context, state) => const FactoriesPage(),
          ),
          GoRoute(
            path: '/home/mines',
            builder: (context, state) => const MinesPage(),
          ),
          GoRoute(
            path: '/home/gardens',
            builder: (context, state) => const GardensPage(),
          ),
          GoRoute(
            path: '/home/market',
            builder: (context, state) => const MarketplacePage(),
          ),
          GoRoute(
            path: '/home/inventory',
            builder: (context, state) => const InventoryPage(),
          ),
          GoRoute(
            path: '/home/social',
            builder: (context, state) => const SocialPage(),
          ),
          GoRoute(
            path: '/home/profile',
            builder: (context, state) => const ProfilePage(),
          ),
          GoRoute(
            path: '/home/reports',
            builder: (context, state) => const ReportsPage(),
          ),
        ],
      ),
      
      // Building Detail Routes (outside ShellRoute for full screen)
      GoRoute(
        path: '/shops/:id',
        builder: (context, state) => BuildingDetailPage(
          buildingId: state.pathParameters['id']!,
          buildingType: 'SHOP',
        ),
      ),
      GoRoute(
        path: '/farms/:id',
        builder: (context, state) => BuildingDetailPage(
          buildingId: state.pathParameters['id']!,
          buildingType: 'FARM',
        ),
      ),
      GoRoute(
        path: '/factories/:id',
        builder: (context, state) => BuildingDetailPage(
          buildingId: state.pathParameters['id']!,
          buildingType: 'FACTORY',
        ),
      ),
      GoRoute(
        path: '/mines/:id',
        builder: (context, state) => BuildingDetailPage(
          buildingId: state.pathParameters['id']!,
          buildingType: 'MINE',
        ),
      ),
      GoRoute(
        path: '/gardens/:id',
        builder: (context, state) => BuildingDetailPage(
          buildingId: state.pathParameters['id']!,
          buildingType: 'GARDEN',
        ),
      ),
      
      // Create Building Wizard
      GoRoute(
        path: '/create-building/:type',
        builder: (context, state) => CreateBuildingWizard(
          buildingType: state.pathParameters['type']!,
        ),
      ),
    ],
    errorBuilder: (context, state) => Scaffold(
      body: Center(
        child: Text('Sayfa bulunamadı: ${state.error}'),
      ),
    ),
  );
});
