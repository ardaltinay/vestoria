import 'package:flutter/material.dart';

/// Emoji map matching Vue.js implementation
/// Updated with comprehensive list of items
const Map<String, String> productEmojis = {
  // --- Raw Resources & Mining ---
  "demir": "⛓️",
  "demir cevheri": "🪨",
  "kömür": "⚫",
  "altın": "🪙",
  "altın cevheri": "✨",
  "gümüş": "🥈",
  "gümüş cevheri": "bijoux",
  "bakır": "🥉",
  "çelik": "🔩",
  "petrol": "🛢️",
  "benzin": "⛽",
  "taş": "🪨",
  "kum": "⏳",
  "kil": "🧱",
  "odun": "🪵",
  "kereste": "🪵",
  "kalas": "📏",

  // --- Farming & Agriculture ---
  "buğday": "🌾",
  "arpa": "🌾",
  "yulaf": "🌾",
  "mısır": "🌽",
  "pamuk": "☁️",
  "saman": "🌾",
  "gübre": "💩",
  "tohum": "🌱",
  "yonca": "🍀",

  // --- Food: Fruits ---
  "elma": "🍎",
  "yeşil elma": "🍏",
  "armut": "🍐",
  "portakal": "🍊",
  "mandalina": "🍊",
  "limon": "🍋",
  "muz": "🍌",
  "karpuz": "🍉",
  "üzüm": "🍇",
  "çilek": "🍓",
  "yaban mersini": "🫐",
  "kavun": "🍈",
  "kiraz": "🍒",
  "şeftali": "🍑",
  "ananas": "🍍",
  "mango": "🥭",
  "kivi": "🥝",
  "hindistan cevizi": "🥥",
  "nar": "🔴",
  "avakado": "🥑",

  // --- Food: Vegetables ---
  "domates": "🍅",
  "patates": "🥔",
  "havuç": "🥕",
  "biber": "🌶️",
  "yeşil biber": "🫑",
  "salatalık": "🥒",
  "marul": "🥬",
  "brokoli": "🥦",
  "sarımsak": "🧄",
  "soğan": "🧅",
  "mantar": "🍄",
  "patlıcan": "🍆",
  "mısır koçanı": "🌽",
  "bezelye": "🫛",
  "kabak": "🎃",

  // --- Food: Animal Products ---
  "süt": "🥛",
  "yumurta": "🥚",
  "bal": "🍯",
  "et": "🥩",
  "biftek": "🥩",
  "sosis": "🌭",
  "salam": "🥓",
  "tavuk": "🍗",
  "tavuk butu": "🍗",
  "hindi": "🦃",
  "balık": "🐟",
  "somon": "🍣",
  "karides": "🦐",
  "yengeç": "🦀",
  "istakoz": "🦞",

  // --- Food: Bakery & Processed ---
  "ekmek": "🍞",
  "baget": "🥖",
  "kruvasan": "🥐",
  "pide": "🫓",
  "simit": "🥯",
  "pancake": "🥞",
  "waffle": "🧇",
  "peynir": "🧀",
  "tereyağı": "🧈",
  "yoğurt": "🥣",
  "krema": "🧁",
  "un": "🥡",
  "şeker": "🍬",
  "tuz": "🧂",
  "zeytinyağı": "🫗",
  "ayçiçek yağı": "🌻",

  // --- Food: Meals ---
  "pizza": "🍕",
  "hamburger": "🍔",
  "sandviç": "🥪",
  "sosisli": "🌭",
  "patates kızartması": "🍟",
  "taco": "🌮",
  "burrito": "🌯",
  "döner": "🥙",
  "kebap": "🍢",
  "makarna": "🍝",
  "pilav": "🍚",
  "çorba": "🥣",
  "salata": "🥗",
  "köfte": "🧆",
  "sushi": "🍣",

  // --- Food: Desserts ---
  "dondurma": "🍦",
  "pasta": "🍰",
  "kek": "🎂",
  "kurabiye": "🍪",
  "çikolata": "🍫",
  "şekerleme": "🍬",
  "lolipop": "🍭",
  "puding": "🍮",
  "baklava": "🍯",
  
  // --- Drinks ---
  "su": "💧",
  "kahve": "☕",
  "çay": "🍵",
  "sütlü kahve": "🧋",
  "meyve suyu": "🧃",
  "portakal suyu": "🍊",
  "limonata": "🍋",
  "bira": "🍺",
  "şarap": "🍷",
  "kola": "🥤",
  "soda": "🥤",
  "ayran": "🥛",

  // --- Textiles & Clothing ---
  "kumaş": "🧵",
  "iplik": "🧶",
  "yün": "🧶",
  "pamuk ipliği": "🧵",
  "deri": "🐄",
  "kürk": "🧥",
  "kıyafet": "👕",
  "tişört": "👕",
  "gömlek": "👔",
  "pantolon": "👖",
  "kot pantolon": "👖",
  "elbise": "👗",
  "etek": "👗",
  "ceket": "🧥",
  "mont": "🧥",
  "ayakkabı": "👞",
  "spor ayakkabı": "👟",
  "topuklu ayakkabı": "👠",
  "bot": "🥾",
  "şapka": "🧢",
  "eldiven": "🧤",
  "atkı": "🧣",
  "çorap": "🧦",
  "çanta": "👜",
  "sırt çantası": "🎒",
  "cüzdan": "👛",
  "kemer": "👖",

  // --- Jewelry & Luxury (Specifics) ---
  "mücevher": "💎",
  "elmas": "💎",
  "zümrüt": "💚",
  "yakut": "❤️",
  "safir": "💙",
  "altın kolye": "📿",
  "gümüş kolye": "📿",
  "inci kolye": "📿",
  "altın yüzük": "💍",
  "gümüş yüzük": "💍",
  "elmas yüzük": "💍",
  "pırlanta yüzük": "💍",
  "küpe": "👂", // or 💎
  "altın küpe": "✨",
  "gümüş küpe": "✨",
  "bilezik": "⭕",
  "altın bilezik": "🟡",
  "saat": "⌚",
  "kol saati": "⌚",
  "lüks saat": "⌚",
  "taç": "👑",

  // --- Electronics & Technology ---
  "elektrik": "⚡",
  "devre kartı": "dk", // 📟
  "işlemci": "🖲️",
  "çip": "💾",
  "bilgisayar": "💻",
  "laptop": "💻",
  "telefon": "📱",
  "akıllı telefon": "📱",
  "tablet": "📱",
  "televizyon": "📺",
  "kamera": "📷",
  "fotoğraf makinesi": "📸",
  "kulaklık": "🎧",
  "hoparlör": "🔊",
  "batarya": "🔋",
  "pil": "🔋",
  "kablo": "🔌",
  "ampul": "💡",

  // --- Home & Furniture ---
  "masa": "🪑",
  "sandalye": "🪑",
  "koltuk": "🛋️",
  "yatak": "🛏️",
  "dolap": "🚪",
  "halı": "🧶",
  "perde": "🪟",
  "lamba": "🛋️",
  "vazo": "🏺",
  "ayna": "🪞",

  // --- Tools & Construction ---
  "çekiç": "🔨",
  "balta": "🪓",
  "kazma": "⛏️",
  "kürek": "cc", // 🥄 or 
  "testere": "🪚",
  "tornavida": "🪛",
  "anahtar": "🔧",
  "çivi": "📍",
  "vida": "🔩",
  "tuğla": "🧱",
  "beton": "aaa", // 🧱
  "sıva": "🧱",
  "boya": "🎨",
  "fırça": "🖌️",

  // --- Vehicles ---
  "araba": "🚗",
  "kamyon": "🚛",
  "tır": "🚛",
  "motosiklet": "🏍️",
  "bisiklet": "🚲",
  "otobüs": "🚌",
  "traktör": "🚜",
  "uçak": "✈️",
  "gemi": "🚢",
  "tekne": "🛥️",
  "lastik": "🛞",
  "motor": "⚙️",

  // --- Nature & Environment ---
  "güneş": "☀️",
  // "su": "💧", // Removed duplicate
  "rüzgar": "💨",
  "toprak": "🌱",
  "ağaç": "🌳",
  "orman": "🌲",
  "çiçek": "🌸",
  "gül": "🌹",
  "lale": "🌷",
  "papatya": "🌼",

  // --- Miscellaneous ---
  "kitap": "📖",
  "kağıt": "📄",
  "kalem": "✏️",
  "kutu": "📦",
  "paket": "📦",
  "mektup": "✉️",
  "para": "💵",
  "oyuncak": "🧸",
  "top": "⚽",
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

  @override
  Widget build(BuildContext context) {
    return Text(
      getProductEmoji(productName),
      style: TextStyle(fontSize: size),
    );
  }
}

/// Helper function to get emoji for a product name
String getProductEmoji(String productName) {
  final searchName = productName.toLowerCase().trim();
  
  // 1. Exact match (Priority)
  if (productEmojis.containsKey(searchName)) {
    return productEmojis[searchName]!;
  }
  
  // 2. Starts with (High correlation)
  // e.g. "Altın Kolye" starts with "Altın" -> might be okay but we prefer specific
  // But wait, the user wants "Gümüş Kolye" NOT to return "🥈" (Gümüş).
  // So we should actually trust exact matches of words.
  
  // Strategy: 
  // - If we have "gümüş kolye" in the map, exact match catches it.
  // - If we have "gümüş" in the map, exact match catches "gümüş".
  // - If input is "gümüş kolye" and NOT in map:
  //   - partial match will find "gümüş".
  //   - user says "don't use silver emoji for silver necklace".
  //   - implying we MUST have specific emoji for necklace.
  //   - IF we don't have it, maybe fallback is better than wrong specific?
  //   - But mostly, providing the data is the solution.
  
  // Let's try to match the *longest* key that is contained in the search name.
  // This helps if we have "elma" and "yeşil elma". Input "taze yeşil elma".
  // "elma" matches. "yeşil elma" matches. "yeşil elma" is longer -> better match.
  
  String? bestMatch;
  int bestMatchLen = 0;

  for (final entry in productEmojis.entries) {
    if (searchName.contains(entry.key)) {
      if (entry.key.length > bestMatchLen) {
        bestMatch = entry.value;
        bestMatchLen = entry.key.length;
      }
    }
  }
  
  if (bestMatch != null) return bestMatch;
  
  return "📦";
}
