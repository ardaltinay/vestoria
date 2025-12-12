import 'package:flutter/material.dart';

/// Currency icon widget matching Vue.js CurrencyIcon.vue
/// Hexagonal coin shape with V and P letters inside
class CurrencyIcon extends StatelessWidget {
  final double size;
  final CurrencyIconVariant variant;

  const CurrencyIcon({
    super.key,
    this.size = 20,
    this.variant = CurrencyIconVariant.defaultVariant,
  });

  @override
  Widget build(BuildContext context) {
    return CustomPaint(
      size: Size(size, size),
      painter: _CurrencyIconPainter(variant: variant),
    );
  }
}

enum CurrencyIconVariant {
  defaultVariant, // amber/gold
  success,        // emerald/green
  primary,        // blue
  muted,          // slate/gray
}

class _CurrencyIconPainter extends CustomPainter {
  final CurrencyIconVariant variant;

  _CurrencyIconPainter({required this.variant});

  Color get bgColor {
    switch (variant) {
      case CurrencyIconVariant.success:
        return const Color(0xFF10b981); // emerald-500
      case CurrencyIconVariant.primary:
        return const Color(0xFF3b82f6); // blue-500
      case CurrencyIconVariant.muted:
        return const Color(0xFF94a3b8); // slate-400
      case CurrencyIconVariant.defaultVariant:
        return const Color(0xFFf59e0b); // amber-500
    }
  }

  Color get strokeColor {
    switch (variant) {
      case CurrencyIconVariant.success:
        return const Color(0xFF059669); // emerald-600
      case CurrencyIconVariant.primary:
        return const Color(0xFF2563eb); // blue-600
      case CurrencyIconVariant.muted:
        return const Color(0xFF64748b); // slate-500
      case CurrencyIconVariant.defaultVariant:
        return const Color(0xFFd97706); // amber-600
    }
  }

  Color get iconColor => Colors.white;

  @override
  void paint(Canvas canvas, Size size) {
    final scale = size.width / 24;
    
    // Hexagonal coin shape path: M12 2L20.66 7V17L12 22L3.34 17V7L12 2Z
    final hexPath = Path();
    hexPath.moveTo(12 * scale, 2 * scale);
    hexPath.lineTo(20.66 * scale, 7 * scale);
    hexPath.lineTo(20.66 * scale, 17 * scale);
    hexPath.lineTo(12 * scale, 22 * scale);
    hexPath.lineTo(3.34 * scale, 17 * scale);
    hexPath.lineTo(3.34 * scale, 7 * scale);
    hexPath.close();

    // Fill
    final fillPaint = Paint()
      ..color = bgColor
      ..style = PaintingStyle.fill;
    canvas.drawPath(hexPath, fillPaint);

    // Stroke
    final strokePaint = Paint()
      ..color = strokeColor
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.5 * scale
      ..strokeJoin = StrokeJoin.round;
    canvas.drawPath(hexPath, strokePaint);

    // V letter path: M8 8L12 16L16 8
    final vPath = Path();
    vPath.moveTo(8 * scale, 8 * scale);
    vPath.lineTo(12 * scale, 16 * scale);
    vPath.lineTo(16 * scale, 8 * scale);

    final letterPaint = Paint()
      ..color = iconColor
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2 * scale
      ..strokeCap = StrokeCap.round
      ..strokeJoin = StrokeJoin.round;
    canvas.drawPath(vPath, letterPaint);

    // P letter path: M12 16V11M12 11H14.5C15.3284 11 16 11.6716 16 12.5C16 13.3284 15.3284 14 14.5 14H12V11Z
    final pPath = Path();
    // Vertical line from 16 to 11
    pPath.moveTo(12 * scale, 16 * scale);
    pPath.lineTo(12 * scale, 11 * scale);
    // Horizontal to right
    pPath.lineTo(14.5 * scale, 11 * scale);
    // Arc for P curve
    pPath.arcToPoint(
      Offset(14.5 * scale, 14 * scale),
      radius: Radius.circular(1.5 * scale),
      clockwise: true,
    );
    pPath.lineTo(12 * scale, 14 * scale);

    final pPaint = Paint()
      ..color = iconColor
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.8 * scale
      ..strokeCap = StrokeCap.round
      ..strokeJoin = StrokeJoin.round;
    canvas.drawPath(pPath, pPaint);
  }

  @override
  bool shouldRepaint(covariant _CurrencyIconPainter oldDelegate) {
    return oldDelegate.variant != variant;
  }
}

/// Currency display widget with icon and formatted amount
class Currency extends StatelessWidget {
  final num amount;
  final double iconSize;
  final CurrencyIconVariant variant;
  final bool showDecimals;
  final bool compact;
  final TextStyle? textStyle;

  const Currency({
    super.key,
    required this.amount,
    this.iconSize = 16,
    this.variant = CurrencyIconVariant.defaultVariant,
    this.showDecimals = false,
    this.compact = false,
    this.textStyle,
  });

  String _formatCurrency(num value) {
    if (compact && value >= 1000000) {
      return '${(value / 1000000).toStringAsFixed(1)}M';
    } else if (compact && value >= 1000) {
      return '${(value / 1000).toStringAsFixed(1)}K';
    } else if (showDecimals) {
      return value.toStringAsFixed(2);
    } else {
      // Add thousands separator
      final intValue = value.toInt();
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
  }

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        CurrencyIcon(size: iconSize, variant: variant),
        const SizedBox(width: 4),
        Text(
          _formatCurrency(amount),
          style: textStyle ?? Theme.of(context).textTheme.bodyMedium,
        ),
      ],
    );
  }
}
