import 'package:equatable/equatable.dart';

class User extends Equatable {
  final String id;
  final String username;
  final String email;
  final double balance;
  final int level;
  final int xp;
  final bool isAdmin;
  final String? role;
  final String? createdTime;
  final DateTime? createdAt;

  const User({
    required this.id,
    required this.username,
    required this.email,
    this.balance = 0,
    this.level = 1,
    this.xp = 0,
    this.isAdmin = false,
    this.role,
    this.createdTime,
    this.createdAt,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    // Backend sends 'isAdmin' as boolean, derive role from it
    final bool adminFlag = json['isAdmin'] == true || json['admin'] == true;
    final String? roleFromJson = json['role'];
    
    return User(
      id: json['id']?.toString() ?? '',
      username: json['username'] ?? '',
      email: json['email'] ?? '',
      balance: (json['balance'] ?? 0).toDouble(),
      level: json['level'] ?? 1,
      xp: json['xp'] ?? 0,
      isAdmin: adminFlag || roleFromJson?.toUpperCase() == 'ADMIN',
      role: roleFromJson ?? (adminFlag ? 'ADMIN' : 'USER'),
      createdTime: json['createdTime'] ?? json['created_time'],
      createdAt: json['createdAt'] != null 
          ? DateTime.tryParse(json['createdAt']) 
          : (json['createdTime'] != null 
              ? DateTime.tryParse(json['createdTime']) 
              : null),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'username': username,
      'email': email,
      'balance': balance,
      'level': level,
      'xp': xp,
      'isAdmin': isAdmin,
      'role': role,
      'createdTime': createdTime,
    };
  }

  // XP required for next level
  int get xpForNextLevel => level * 100;
  
  // XP progress percentage
  double get xpProgress => xp / xpForNextLevel;

  @override
  List<Object?> get props => [id, username, email, balance, level, xp, isAdmin, role, createdTime];
}
