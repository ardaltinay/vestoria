import 'dart:io';
import 'package:dio/dio.dart';

class ApiClient {
  // Android emulator uses 10.0.2.2 to access host's localhost
  // iOS simulator uses localhost directly
  static String get baseUrl {
    if (Platform.isAndroid) {
      return 'http://10.0.2.2:8081/api';
    }
    return 'http://localhost:8081/api';
  }
  
  static final ApiClient _instance = ApiClient._internal();
  static String? _accessToken;
  static String? _csrfToken;
  
  late final Dio _dio;
  
  factory ApiClient() {
    return _instance;
  }
  
  ApiClient._internal() {
    _dio = Dio(BaseOptions(
      baseUrl: baseUrl,
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 10),
      headers: {
        'Content-Type': 'application/json',
      },
    ));
    
    // Interceptor for auth token, CSRF token and logging
    _dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) async {
        // Build cookie header with both tokens
        List<String> cookies = [];
        if (_accessToken != null) {
          cookies.add('accessToken=$_accessToken');
        }
        if (_csrfToken != null) {
          cookies.add('XSRF-TOKEN=$_csrfToken');
        }
        if (cookies.isNotEmpty) {
          options.headers['Cookie'] = cookies.join('; ');
        }
        
        // Add CSRF token as header for mutating requests (POST, PUT, DELETE, PATCH)
        if (_csrfToken != null && 
            ['POST', 'PUT', 'DELETE', 'PATCH'].contains(options.method.toUpperCase())) {
          options.headers['X-XSRF-TOKEN'] = _csrfToken;
        }
        
        print('API Request: ${options.method} ${options.path}');
        print('Auth token present: ${_accessToken != null}, CSRF token present: ${_csrfToken != null}');
        return handler.next(options);
      },
      onResponse: (response, handler) async {
        print('API Response: ${response.statusCode} ${response.requestOptions.path}');
        
        // Extract tokens from Set-Cookie header
        final setCookies = response.headers['set-cookie'];
        if (setCookies != null) {
          for (final setCookie in setCookies) {
            // Extract accessToken
            if (setCookie.startsWith('accessToken=')) {
              final parts = setCookie.split(';');
              final tokenPart = parts[0];
              _accessToken = tokenPart.substring('accessToken='.length);
              final preview = _accessToken!.length > 20 ? '${_accessToken!.substring(0, 20)}...' : _accessToken;
              print('Access token extracted: $preview');
            }
            // Extract XSRF-TOKEN (CSRF token)
            if (setCookie.startsWith('XSRF-TOKEN=')) {
              final parts = setCookie.split(';');
              final tokenPart = parts[0];
              _csrfToken = tokenPart.substring('XSRF-TOKEN='.length);
              final preview = _csrfToken!.length > 20 ? '${_csrfToken!.substring(0, 20)}...' : _csrfToken;
              print('CSRF token extracted: $preview');
            }
          }
        }
        return handler.next(response);
      },
      onError: (error, handler) async {
        print('API Error: ${error.response?.statusCode} ${error.requestOptions.path}');
        print('Error data: ${error.response?.data}');
        return handler.next(error);
      },
    ));
  }
  
  Dio get dio => _dio;
  
  // Clear tokens on logout
  void clearToken() {
    _accessToken = null;
    _csrfToken = null;
  }
  
  // Fetch CSRF token from backend (non-blocking, fire and forget)
  Future<void> fetchCsrfToken() async {
    if (_csrfToken != null) return; // Already have token
    
    try {
      // Create a separate dio instance with short timeout to avoid blocking
      final csrfDio = Dio(BaseOptions(
        baseUrl: baseUrl,
        connectTimeout: const Duration(seconds: 3),
        receiveTimeout: const Duration(seconds: 3),
        headers: {'Content-Type': 'application/json'},
      ));
      
      // Add auth cookie if available
      if (_accessToken != null) {
        csrfDio.options.headers['Cookie'] = 'accessToken=$_accessToken';
      }
      
      final response = await csrfDio.get('/auth/csrf');
      
      // Extract token from response body
      if (response.data != null && response.data['token'] != null) {
        _csrfToken = response.data['token'];
        print('CSRF token fetched: ${_csrfToken?.substring(0, 20)}...');
      }
      
      // Also check Set-Cookie header
      final setCookies = response.headers['set-cookie'];
      if (setCookies != null) {
        for (final setCookie in setCookies) {
          if (setCookie.startsWith('XSRF-TOKEN=')) {
            final parts = setCookie.split(';');
            final tokenPart = parts[0];
            _csrfToken = tokenPart.substring('XSRF-TOKEN='.length);
            print('CSRF token from cookie: ${_csrfToken?.substring(0, 20)}...');
          }
        }
      }
    } catch (e) {
      print('CSRF token fetch skipped: $e');
    }
  }
  
  // Auth
  Future<Response> login(String username, String password) async {
    final response = await _dio.post('/auth/login', data: {
      'username': username,
      'password': password,
    });
    
    // Fire and forget - don't block login on CSRF fetch
    fetchCsrfToken();
    
    return response;
  }
  
  Future<Response> register(String username, String email, String password) {
    return _dio.post('/auth/register', data: {
      'username': username,
      'email': email,
      'password': password,
    });
  }
  
  Future<Response> logout() async {
    final response = await _dio.post('/auth/logout');
    clearToken();
    return response;
  }
  
  Future<Response> getCurrentUser() {
    return _dio.get('/users/me');
  }
  
  // Dashboard
  Future<Response> getDashboardStats() {
    return _dio.get('/dashboard-stats');
  }
  
  // Buildings - Backend uses /build/list for all buildings
  Future<Response> getAllBuildings() {
    return _dio.get('/build/list');
  }
  
  // Convenience methods that filter by building type
  Future<List<Map<String, dynamic>>> getShops() async {
    final response = await getAllBuildings();
    final List allBuildings = response.data ?? [];
    return allBuildings
        .where((b) => b['type'] == 'SHOP')
        .map<Map<String, dynamic>>((b) => Map<String, dynamic>.from(b))
        .toList();
  }
  
  Future<List<Map<String, dynamic>>> getFarms() async {
    final response = await getAllBuildings();
    final List allBuildings = response.data ?? [];
    return allBuildings
        .where((b) => b['type'] == 'FARM')
        .map<Map<String, dynamic>>((b) => Map<String, dynamic>.from(b))
        .toList();
  }
  
  Future<List<Map<String, dynamic>>> getFactories() async {
    final response = await getAllBuildings();
    final List allBuildings = response.data ?? [];
    return allBuildings
        .where((b) => b['type'] == 'FACTORY')
        .map<Map<String, dynamic>>((b) => Map<String, dynamic>.from(b))
        .toList();
  }
  
  Future<List<Map<String, dynamic>>> getMines() async {
    final response = await getAllBuildings();
    final List allBuildings = response.data ?? [];
    return allBuildings
        .where((b) => b['type'] == 'MINE')
        .map<Map<String, dynamic>>((b) => Map<String, dynamic>.from(b))
        .toList();
  }
  
  Future<List<Map<String, dynamic>>> getGardens() async {
    final response = await getAllBuildings();
    final List allBuildings = response.data ?? [];
    return allBuildings
        .where((b) => b['type'] == 'GARDEN')
        .map<Map<String, dynamic>>((b) => Map<String, dynamic>.from(b))
        .toList();
  }
  
  // Marketplace
  Future<Response> getMarketListings() {
    return _dio.get('/market/listings');
  }
  
  Future<Response> createMarketListing(Map<String, dynamic> data) {
    return _dio.post('/markets', data: data);
  }
  
  Future<Response> buyMarketListing(String id, {int quantity = 1}) {
    return _dio.post('/market/buy/$id', data: {'quantity': quantity});
  }
  
  // Inventory - centralized (items not assigned to any building)
  Future<Response> getInventory() {
    return _dio.get('/inventory/centralized');
  }
  
  // Announcements
  Future<Response> getAnnouncements() {
    return _dio.get('/announcements');
  }
  
  // Game Data
  Future<Response> getGameData() {
    return _dio.get('/game-data/items');
  }
  
  Future<Response> getBuildingConfigs() {
    return _dio.get('/game-data/buildings');
  }
  
  // Building Detail
  Future<Response> getBuildingById(String id) {
    return _dio.get('/build/$id');
  }
  
  // Building Actions
  Future<Response> startSales(String buildingId) {
    return _dio.post('/build/$buildingId/start-sales');
  }
  
  Future<Response> completeSale(String buildingId) {
    return _dio.post('/build/$buildingId/complete-sale');
  }
  
  Future<Response> startProduction(String buildingId, String productId) {
    return _dio.post('/build/$buildingId/start-production', data: {'productId': productId});
  }
  
  Future<Response> collectProduction(String buildingId) {
    return _dio.post('/build/$buildingId/collect');
  }
  
  Future<Response> upgradeBuilding(String buildingId) {
    return _dio.put('/build/upgrade/$buildingId');
  }
  
  Future<Response> closeBuilding(String buildingId) {
    return _dio.delete('/build/close/$buildingId');
  }
  
  Future<Response> transferToBuilding(String buildingId, String itemName, int quantity) {
    return _dio.post('/build/$buildingId/transfer', data: {
      'itemName': itemName,
      'quantity': quantity,
    });
  }
  
  Future<Response> withdrawFromBuilding(String buildingId, String itemName, int quantity) {
    return _dio.post('/build/$buildingId/withdraw', data: {
      'itemName': itemName,
      'quantity': quantity,
    });
  }
}


