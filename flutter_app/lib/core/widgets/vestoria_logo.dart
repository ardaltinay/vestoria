import 'package:flutter/material.dart';
import '../theme/app_theme.dart';

/// Vestoria V-shaped logo widget matching the web favicon.svg
class VestoriaLogo extends StatelessWidget {
  final double size;
  final bool showText;
  final bool useWhite;
  
  const VestoriaLogo({
    super.key,
    this.size = 40,
    this.showText = false,
    this.useWhite = false,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        CustomPaint(
          size: Size(size, size),
          painter: _VestoriaLogoPainter(useWhite: useWhite),
        ),
        if (showText) ...[
          const SizedBox(width: 12),
          Text(
            'Vestoria',
            style: Theme.of(context).textTheme.headlineSmall?.copyWith(
              fontWeight: FontWeight.bold,
              color: useWhite ? Colors.white : null,
            ),
          ),
        ],
      ],
    );
  }
}

class _VestoriaLogoPainter extends CustomPainter {
  final bool useWhite;
  
  _VestoriaLogoPainter({this.useWhite = false});
  
  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()..style = PaintingStyle.fill;
    
    final mainColor = useWhite ? Colors.white : AppColors.primary;
    final lightColor = useWhite ? Colors.white.withOpacity(0.3) : AppColors.primary.withOpacity(0.2);
    final dotColor = useWhite ? Colors.white70 : AppColors.slate500;
    
    // Background V shape (lighter)
    paint.color = lightColor;
    final bgPath = Path()
      ..moveTo(size.width * 0.5, size.height)        // Bottom center
      ..lineTo(0, 0)                                   // Top left
      ..lineTo(size.width * 0.3, 0)                   // Top left inner
      ..lineTo(size.width * 0.5, size.height * 0.5)  // Middle
      ..lineTo(size.width * 0.7, 0)                   // Top right inner
      ..lineTo(size.width, 0)                          // Top right
      ..close();
    canvas.drawPath(bgPath, paint);
    
    // Foreground V shape (solid)
    paint.color = mainColor;
    final fgPath = Path()
      ..moveTo(size.width * 0.5, size.height)         // Bottom center
      ..lineTo(size.width * 0.2, size.height * 0.25) // Left
      ..lineTo(size.width * 0.4, size.height * 0.25) // Left inner
      ..lineTo(size.width * 0.5, size.height * 0.55) // Middle
      ..lineTo(size.width * 0.6, size.height * 0.25) // Right inner
      ..lineTo(size.width * 0.8, size.height * 0.25) // Right
      ..close();
    canvas.drawPath(fgPath, paint);
    
    // Top circle (dot)
    paint.color = dotColor;
    canvas.drawCircle(
      Offset(size.width * 0.5, size.height * 0.2),
      size.width * 0.1,
      paint,
    );
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

