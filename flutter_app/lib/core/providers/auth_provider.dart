import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../api/api_client.dart';
import '../../data/models/user.dart';

final authProvider = StateNotifierProvider<AuthNotifier, AuthState>((ref) {
  return AuthNotifier();
});

class AuthState {
  final User? user;
  final bool isLoading;
  final String? error;
  final bool isAuthenticated;

  AuthState({
    this.user,
    this.isLoading = false,
    this.error,
    this.isAuthenticated = false,
  });

  AuthState copyWith({
    User? user,
    bool? isLoading,
    String? error,
    bool? isAuthenticated,
  }) {
    return AuthState(
      user: user ?? this.user,
      isLoading: isLoading ?? this.isLoading,
      error: error ?? this.error,
      isAuthenticated: isAuthenticated ?? this.isAuthenticated,
    );
  }
}

class AuthNotifier extends StateNotifier<AuthState> {
  final ApiClient _api = ApiClient();

  AuthNotifier() : super(AuthState()) {
    // Don't auto-load on startup - user needs to login
    state = state.copyWith(isLoading: false);
  }

  Future<bool> login(String username, String password) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = await _api.login(username, password);
      final user = User.fromJson(response.data);
      
      state = state.copyWith(
        user: user,
        isAuthenticated: true,
        isLoading: false,
      );
      return true;
    } catch (e) {
      print('Login error: $e');
      state = state.copyWith(
        isLoading: false,
        error: 'Giriş başarısız. Lütfen bilgilerinizi kontrol edin.',
      );
      return false;
    }
  }

  Future<bool> register(String username, String email, String password) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = await _api.register(username, email, password);
      final user = User.fromJson(response.data);
      
      state = state.copyWith(
        user: user,
        isAuthenticated: true,
        isLoading: false,
      );
      return true;
    } catch (e) {
      print('Register error: $e');
      state = state.copyWith(
        isLoading: false,
        error: 'Kayıt başarısız. Lütfen bilgilerinizi kontrol edin.',
      );
      return false;
    }
  }

  Future<void> logout() async {
    state = state.copyWith(isLoading: true);
    try {
      await _api.logout();
    } catch (e) {
      print('Logout error: $e');
    }
    state = AuthState(); // Reset to initial state
  }

  Future<void> refreshUser() async {
    try {
      final response = await _api.getCurrentUser();
      final user = User.fromJson(response.data);
      state = state.copyWith(user: user);
    } catch (_) {}
  }
}
