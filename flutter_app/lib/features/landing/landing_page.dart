import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../../core/theme/app_theme.dart';
import '../../core/widgets/vestoria_logo.dart';
import '../../core/widgets/building_icons.dart';

class LandingPage extends StatelessWidget {
  const LandingPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.slate900,
      body: SafeArea(
        child: Center(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                // Logo
                Container(
                  width: 100,
                  height: 100,
                  decoration: BoxDecoration(
                    color: AppColors.primary,
                    borderRadius: BorderRadius.circular(24),
                    boxShadow: [
                      BoxShadow(
                        color: AppColors.primary.withOpacity(0.4),
                        blurRadius: 30,
                        spreadRadius: 5,
                      ),
                    ],
                  ),
                  child: const Center(
                    child: VestoriaLogo(size: 60, useWhite: true),
                  ),
                ),
                const SizedBox(height: 32),
                
                // Title
                Text(
                  'VESTORIA',
                  style: Theme.of(context).textTheme.displayMedium?.copyWith(
                    color: Colors.white,
                    fontWeight: FontWeight.bold,
                    letterSpacing: 4,
                  ),
                ),
                const SizedBox(height: 12),
                
                // Subtitle
                Text(
                  'İş imparatorluğunu kur',
                  style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                    color: AppColors.slate400,
                  ),
                ),
                const SizedBox(height: 48),
                
                // Buttons
                ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 300),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      SizedBox(
                        height: 52,
                        child: ElevatedButton(
                          onPressed: () => context.go('/register'),
                          child: const Text('Başla'),
                        ),
                      ),
                      const SizedBox(height: 12),
                      SizedBox(
                        height: 52,
                        child: OutlinedButton(
                          style: OutlinedButton.styleFrom(
                            foregroundColor: Colors.white,
                            side: BorderSide(color: AppColors.slate600),
                          ),
                          onPressed: () => context.go('/login'),
                          child: const Text('Giriş Yap'),
                        ),
                      ),
                    ],
                  ),
                ),
                
                const SizedBox(height: 48),
                
                // Features
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    _FeatureChip(emoji: BuildingIcons.shopEmoji, label: 'Dükkanlar'),
                    const SizedBox(width: 12),
                    _FeatureChip(emoji: BuildingIcons.farmEmoji, label: 'Çiftlikler'),
                    const SizedBox(width: 12),
                    _FeatureChip(emoji: BuildingIcons.factoryEmoji, label: 'Fabrikalar'),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _FeatureChip extends StatelessWidget {
  final String emoji;
  final String label;

  const _FeatureChip({required this.emoji, required this.label});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      decoration: BoxDecoration(
        color: AppColors.slate800,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: AppColors.slate700),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(emoji, style: const TextStyle(fontSize: 16)),
          const SizedBox(width: 6),
          Text(
            label,
            style: Theme.of(context).textTheme.bodySmall?.copyWith(
              color: AppColors.slate300,
            ),
          ),
        ],
      ),
    );
  }
}
