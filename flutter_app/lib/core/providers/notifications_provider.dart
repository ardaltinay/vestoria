import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../api/api_client.dart';

// Notification model
class AppNotification {
  final String id;
  final String message;
  final bool isRead;
  final DateTime createdAt;

  AppNotification({
    required this.id,
    required this.message,
    required this.isRead,
    required this.createdAt,
  });

  factory AppNotification.fromJson(Map<String, dynamic> json) {
    return AppNotification(
      id: json['id']?.toString() ?? '',
      message: json['message'] ?? '',
      isRead: json['isRead'] ?? json['read'] ?? false,
      createdAt: json['createdAt'] != null 
          ? DateTime.tryParse(json['createdAt']) ?? DateTime.now()
          : DateTime.now(),
    );
  }
}

// Notifications provider
final notificationsProvider = FutureProvider<List<AppNotification>>((ref) async {
  final api = ApiClient();
  try {
    final response = await api.getNotifications();
    final dynamic data = response.data;
    List contentList;
    
    if (data is Map && data['content'] != null) {
      contentList = data['content'] as List;
    } else if (data is List) {
      contentList = data;
    } else {
      contentList = [];
    }
    
    return contentList
        .map((n) => AppNotification.fromJson(Map<String, dynamic>.from(n)))
        .toList();
  } catch (e) {
    print('Notifications error: $e');
    return [];
  }
});

// Unread count provider
final unreadNotificationsCountProvider = Provider<int>((ref) {
  final notifications = ref.watch(notificationsProvider);
  return notifications.when(
    data: (list) => list.where((n) => !n.isRead).length,
    loading: () => 0,
    error: (_, __) => 0,
  );
});
