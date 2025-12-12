import 'package:flutter/material.dart';

/// Emoji map matching Vue.js implementation
const Map<String, String> productEmojis = {
  // Grains & Bakery
  "buğday": "🌾",
  "ekmek": "🍞",
  "un": "🥡",
  
  // Metals & Mining
  "demir": "⛓️",
  "kömür": "⚫",
  "altın": "🪙",
  "çelik": "🔩",
  "bakır": "🥉",
  "gümüş": "🥈",
  
  // Dairy & Eggs
  "süt": "🥛",
  "yumurta": "🥚",
  "peynir": "🧀",
  "tereyağı": "🧈",
  
  // Textiles & Clothing
  "kumaş": "🧵",
  "kıyafet": "👕",
  "deri": "🐄",
  "çanta": "👜",
  "ayakkabı": "👞",
  "ceket": "🧥",
  "pantolon": "👖",
  "gömlek": "👔",
  "elbise": "👗",
  
  // Jewelry
  "mücevher": "💎",
  "kolye": "📿",
  "yüzük": "💍",
  "küpe": "💎",
  
  // Tools & Equipment
  "makine parçası": "⚙️",
  "çekiç": "🔨",
  "tornavida": "🪛",
  "anahtar": "🔧",
  "testere": "🪚",
  
  // Construction
  "tuğla": "🧱",
  "odun": "🪵",
  "mobilya": "🪑",
  
  // Electronics
  "elektrik": "⚡",
  "ampul": "💡",
  "priz": "🔌",
  "akü": "🔋",
  
  // Fruits
  "elma": "🍎",
  "armut": "🍐",
  "portakal": "🍊",
  "mandalina": "🍊",
  "limon": "🍋",
  "muz": "🍌",
  "karpuz": "🍉",
  "üzüm": "🍇",
  "çilek": "🍓",
  "kavun": "🍈",
  "kiraz": "🍒",
  "şeftali": "🍑",
  "ananas": "🍍",
  "mango": "🥭",
  "kivi": "🥝",
  
  // Vegetables
  "domates": "🍅",
  "patates": "🥔",
  "havuç": "🥕",
  "mısır": "🌽",
  "biber": "🌶️",
  "salatalık": "🥒",
  "marul": "🥬",
  "brokoli": "🥦",
  "sarımsak": "🧄",
  "soğan": "🧅",
  "mantar": "🍄",
  "sebze": "🥦",
  
  // Meat & Fish
  "et": "🥩",
  "balık": "🐟",
  "tavuk": "🍗",
  "hindi": "🦃",
  "sığır": "🐄",
  "koyun": "🐑",
  "keçi": "🐐",
  
  // Flowers & Plants
  "çiçek": "🌸",
  "ayçiçeği": "🌻",
  "ağaç": "🌳",
  "çimen": "🌿",
  "yaprak": "🍃",
  "toprak": "🌱",
  
  // Drinks
  "kahve": "☕",
  "çay": "🍵",
  "meyve suyu": "🧃",
  "ayran": "🥛",
  "limonata": "🍋",
  "su": "💧",
  
  // Food
  "bal": "🍯",
  "şeker": "🍬",
  "çikolata": "🍫",
  "pasta": "🍰",
  "kurabiye": "🍪",
  "dondurma": "🍦",
  "pizza": "🍕",
  "hamburger": "🍔",
  "sandviç": "🥪",
  "makarna": "🍝",
  "pilav": "🍚",
  "çorba": "🥣",
  "salata": "🥗",
  
  // Nuts & Seeds
  "fındık": "🌰",
  "fıstık": "🥜",
  
  // Weather & Nature
  "güneş": "☀️",
  "ay": "🌙",
  "yıldız": "⭐",
  "bulut": "☁️",
  "yağmur": "🌧️",
  "kar": "❄️",
  "ateş": "🔥",
};

/// Widget to display product emoji based on name
class ProductEmoji extends StatelessWidget {
  final String productName;
  final double size;
  
  const ProductEmoji({
    super.key,
    required this.productName,
    this.size = 24,
  });

  String _getEmoji() {
    final searchName = productName.toLowerCase().trim();
    
    // Exact match
    if (productEmojis.containsKey(searchName)) {
      return productEmojis[searchName]!;
    }
    
    // Partial match - check if product name contains any key
    for (final entry in productEmojis.entries) {
      if (searchName.contains(entry.key) || entry.key.contains(searchName)) {
        return entry.value;
      }
    }
    
    // Default emoji
    return "📦";
  }

  @override
  Widget build(BuildContext context) {
    return Text(
      _getEmoji(),
      style: TextStyle(fontSize: size),
    );
  }
}

/// Helper function to get emoji for a product name
String getProductEmoji(String productName) {
  final searchName = productName.toLowerCase().trim();
  
  // Exact match
  if (productEmojis.containsKey(searchName)) {
    return productEmojis[searchName]!;
  }
  
  // Partial match
  for (final entry in productEmojis.entries) {
    if (searchName.contains(entry.key) || entry.key.contains(searchName)) {
      return entry.value;
    }
  }
  
  return "📦";
}
