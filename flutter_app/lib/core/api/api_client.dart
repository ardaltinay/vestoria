import 'dart:io';
import 'dart:math';
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
  static final Random _random = Random.secure();
  
  late final Dio _dio;
  
  factory ApiClient() {
    return _instance;
  }
  
  /// Extracts a user-friendly error message from a DioException or any error
  /// Tries to get message from backend response, falls back to status-based messages
  static String getErrorMessage(dynamic error) {
    if (error is DioException) {
      // Try to get message from backend response
      final data = error.response?.data;
      if (data != null) {
        // Backend may return message in different formats
        if (data is Map) {
          // 1. Try 'message' key (Standard)
          if (data['message'] != null && data['message'].toString().isNotEmpty) {
            return data['message'].toString();
          }
          // 2. Try 'error' key
          if (data['error'] != null) {
            final err = data['error'];
            if (err is String && err.isNotEmpty) return err;
            if (err is Map && err['message'] != null) return err['message'].toString();
          }
          // 3. Try 'detail' key (Spring/Validation)
          if (data['detail'] != null && data['detail'].toString().isNotEmpty) {
            return data['detail'].toString();
          }
        } else if (data is String && data.isNotEmpty) {
          // Sometimes backend returns plain string error
          return data;
        }
      }
      
      // Fall back to status-based messages
      final statusCode = error.response?.statusCode;
      switch (statusCode) {
        case 400:
          return 'Geçersiz istek (400)';
        case 401:
          return 'Oturum süresi doldu, lütfen tekrar giriş yapın';
        case 403:
          return 'Bu işlem için yetkiniz yok';
        case 404:
          return 'İstenen kaynak bulunamadı';
        case 409:
          return 'Çakışma oluştu, lütfen sayfayı yenileyin';
        case 422:
          return 'Veri doğrulama hatası';
        case 500:
          return 'Sunucu hatası (500). Lütfen daha sonra tekrar deneyin.';
        case 502:
        case 503:
        case 504:
          return 'Sunucuya ulaşılamıyor. Bakım çalışması olabilir.';
        default:
          if (error.type == DioExceptionType.connectionTimeout ||
              error.type == DioExceptionType.receiveTimeout ||
              error.type == DioExceptionType.sendTimeout) {
            return 'Bağlantı zaman aşımı. İnternet bağlantınızı kontrol edin.';
          }
          if (error.type == DioExceptionType.connectionError) {
            return 'Bağlantı hatası. Sunucuya erişilemiyor.';
          }
          return 'Bir hata oluştu (${statusCode ?? "bilinmeyen"})';
      }
    }
    
    // For non-Dio errors
    if (error is String) {
      return error;
    }
    return error?.toString() ?? 'Beklenmeyen bir hata oluştu';
  }
  
  // Generate UUID v4 for Idempotency-Key
  static String _generateUUID() {
    const hexDigits = '0123456789abcdef';
    final uuid = StringBuffer();
    for (int i = 0; i < 36; i++) {
      if (i == 8 || i == 13 || i == 18 || i == 23) {
        uuid.write('-');
      } else if (i == 14) {
        uuid.write('4'); // UUID version 4
      } else if (i == 19) {
        uuid.write(hexDigits[(_random.nextInt(4) + 8)]); // Variant
      } else {
        uuid.write(hexDigits[_random.nextInt(16)]);
      }
    }
    return uuid.toString();
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
        
        // Add CSRF token and Idempotency-Key for mutating requests (POST, PUT, DELETE, PATCH)
        if (['POST', 'PUT', 'DELETE', 'PATCH'].contains(options.method.toUpperCase())) {
          if (_csrfToken != null) {
            options.headers['X-XSRF-TOKEN'] = _csrfToken;
          }
          // Generate unique idempotency key for each request
          options.headers['Idempotency-Key'] = _generateUUID();
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
  // Dedup logic like Vue.js BuildingService
  static Future<Response>? _buildingsPromise;
  static DateTime? _lastBuildingsCall;
  
  Future<Response> getAllBuildings() {
    // Dedup concurrent calls - return existing promise if one is pending
    if (_buildingsPromise != null) {
      return _buildingsPromise!;
    }
    
    // Throttle: minimum 500ms between calls
    final now = DateTime.now();
    if (_lastBuildingsCall != null) {
      final elapsed = now.difference(_lastBuildingsCall!);
      if (elapsed.inMilliseconds < 500) {
        // Return cached response if called too quickly
        print('Throttled getAllBuildings - too soon');
        return Future.value(Response(
          requestOptions: RequestOptions(path: '/build/list'),
          statusCode: 429,
          data: {'error': 'Throttled'},
        ));
      }
    }
    _lastBuildingsCall = now;
    
    _buildingsPromise = _dio.get('/build/list').whenComplete(() {
      // Clear promise after 100ms to allow subsequent refreshes
      Future.delayed(const Duration(milliseconds: 100), () {
        _buildingsPromise = null;
      });
    });
    
    return _buildingsPromise!;
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
  
  Future<Response> createMarketListing(String itemId, {required int quantity, required double price}) {
    return _dio.post('/market/list/$itemId', data: {
      'quantity': quantity,
      'price': price,
    });
  }
  
  Future<Response> buyMarketListing(String id, {int quantity = 1}) {
    return _dio.post('/market/buy/$id', data: {'quantity': quantity});
  }
  
  Future<Response> cancelMarketListing(String id) {
    return _dio.delete('/market/listings/$id');
  }
  
  Future<Response> getMarketTrends() {
    return _dio.get('/market/trends');
  }
  
  // Inventory - centralized (items not assigned to any building)
  Future<Response> getInventory() {
    return _dio.get('/inventory/centralized');
  }
  
  // Announcements
  Future<Response> getAnnouncements() {
    return _dio.get('/announcements');
  }

  Future<Response> createAnnouncement(String title, String content, String type) {
    return _dio.post('/announcements/create', data: {
      'title': title.trim(),
      'content': content.trim(),
      'type': type,
    });
  }
  
  // Notifications
  Future<Response> getNotifications() {
    return _dio.get('/notifications');
  }
  
  Future<Response> markNotificationRead(String id) {
    return _dio.post('/notifications/$id/read');
  }
  
  Future<Response> markAllNotificationsRead() {
    return _dio.post('/notifications/read-all');
  }
  
  // Building withdraw - transfer items from building to central inventory
  Future<Response> withdrawFromBuilding(String buildingId, String itemId, int quantity) {
    return _dio.post('/build/$buildingId/withdraw', data: {
      'productId': itemId, // Backend expects 'productId' key in DTO, but we send UUID now
      'quantity': quantity,
    });
  }
  
  // Game Data
  Future<Response> getGameData() {
    return _dio.get('/game-data/items');
  }
  
  Future<Response> getBuildingConfigs() {
    return _dio.get('/build/config');
  }
  
  // Building Detail
  Future<Response> getBuildingById(String id) {
    return _dio.get('/build/$id');
  }
  
  // Create Building
  Future<Response> createBuilding({
    required String type,
    required String tier,
    String? subType, // Nullable - non-SHOP buildings send null
    required String name,
  }) {
    final data = <String, dynamic>{
      'type': type,
      'tier': tier,
      'name': name,
    };
    if (subType != null) {
      data['subType'] = subType;
    }
    return _dio.post('/build/create', data: data);
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
  
  Future<Response> updateItemPrice(String itemId, double price) {
    return _dio.put('/inventory/item/$itemId/price', data: {
      'price': price,
    });
  }
  
  // Transfer from inventory to building
  Future<Response> transferFromInventory(String itemId, String buildingId, int quantity) {
    return _dio.post('/inventory/transfer', data: {
      'itemId': itemId,
      'buildingId': buildingId,
      'quantity': quantity,
    });
  }
}


