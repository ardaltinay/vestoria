package io.vestoria.constant;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class Constants {
    // Bahçe Ürünleri
    public static final List<String> GARDEN_ITEMS = List.of("Zeytin", "Havuç", "Patates", "Domates", "Salatalık",
            "Biber", "Patlıcan", "Soğan", "Elma", "Armut", "Mandalin", "Portakal", "Çilek", "Kiraz", "Şeftali", "Limon",
            "Karpuz", "Kavun");

    // Çiftlik Ürünleri
    public static final List<String> FARM_ITEMS = List.of("İnek", "Koyun", "Keçi", "Deri", "Süt", "Yumurta", "Tavuk",
            "Hindi", "Yün", "Pamuk", "Buğday", "Mısır", "Arpa", "Tütün", "Ayçiçeği", "Kakao");

    // Maden Ürünleri
    public static final List<String> MINE_ITEMS = List.of("Kömür", "Demir", "Bakır", "Altın", "Gümüş", "Petrol", "Taş",
            "Tuz");

    // Fabrika Ürünleri ve Gereksinimleri (Ürün -> Hammaddeler)
    public static final Map<String, List<String>> FACTORY_MAP = Map.ofEntries(
            // Süt Ürünleri
            Map.entry("Peynir", List.of("Süt")), Map.entry("Yoğurt", List.of("Süt")),

            // Tahıl Ürünleri
            Map.entry("Ekmek", List.of("Un")), Map.entry("Un", List.of("Buğday")),
            Map.entry("Ayçiçek Yağı", List.of("Ayçiçeği")),

            // İçecekler
            Map.entry("Meyve Suyu", List.of("Meyve")), Map.entry("Bira", List.of("Arpa")),

            // Et Ürünleri
            Map.entry("Sucuk", List.of("Et")), Map.entry("Salam", List.of("Et")),

            // Tekstil Ürünleri
            Map.entry("Kumaş", List.of("Yün")), Map.entry("Kıyafet", List.of("Pamuk")),
            Map.entry("Ceket", List.of("Deri", "Pamuk")), Map.entry("Ayakkabı", List.of("Deri")),

            // Metal Ürünleri
            Map.entry("Demir Levha", List.of("Demir")), Map.entry("Alet Ekipman", List.of("Çelik")),
            Map.entry("Demir Çelik", List.of("Kömür", "Demir")),

            // Kimyasal Ürünler
            Map.entry("Plastik", List.of("Petrol")), Map.entry("Sabun", List.of("Bitkisel Yağ")),

            // Tatlı ve Atıştırmalıklar
            Map.entry("Çikolata", List.of("Kakao", "Süt")),

            // Kuyumcu Ürünleri
            Map.entry("Kuyumcu Malzemeleri", List.of("Altın", "Gümüş")),

            // Seramik
            Map.entry("Çömlek", List.of("Bakır")),

            // Tütün Ürünleri
            Map.entry("Sigara", List.of("Tütün")), Map.entry("Puro", List.of("Tütün")));

    // Market Ürünleri
    public static final List<String> MARKET_ITEMS = List.of("Tuz", "Sigara", "Puro", "Ekmek", "Meyve Suyu", "Peynir",
            "Yoğurt", "Zeytin", "Un", "Sucuk", "Salam", "Sabun", "Çikolata", "Bira", "Çömlek", "Su");

    // Manav Ürünleri
    public static final List<String> GREENGROCER_ITEMS = List.of("Havuç", "Patates", "Domates", "Salatalık", "Biber",
            "Patlıcan", "Soğan", "Elma", "Armut", "Mandalin", "Portakal", "Çilek", "Kiraz", "Şeftali", "Limon",
            "Karpuz", "Kavun");

    // Giyim Mağazası Ürünleri
    public static final List<String> CLOTHING_ITEMS = List.of("Kumaş", "Kıyafet", "Ceket", "Ayakkabı");

    // Kuyumcu Ürünleri
    public static final List<String> JEWELER_ITEMS = List.of("Bilezik", "Kolye", "Küpe", "Yüzük");

    // Base Prices for Algorithm Reference
    public static final Map<String, BigDecimal> BASE_PRICES = Map.ofEntries(
            // Bahçe (Garden) - Low tier
            Map.entry("Havuç", BigDecimal.valueOf(5)), Map.entry("Patates", BigDecimal.valueOf(5)),
            Map.entry("Domates", BigDecimal.valueOf(6)), Map.entry("Salatalık", BigDecimal.valueOf(6)),
            Map.entry("Soğan", BigDecimal.valueOf(5)), Map.entry("Biber", BigDecimal.valueOf(7)),
            Map.entry("Patlıcan", BigDecimal.valueOf(7)), Map.entry("Karpuz", BigDecimal.valueOf(10)),
            Map.entry("Kavun", BigDecimal.valueOf(10)),
            // Garden - Fruits
            Map.entry("Elma", BigDecimal.valueOf(8)), Map.entry("Armut", BigDecimal.valueOf(8)),
            Map.entry("Mandalin", BigDecimal.valueOf(9)), Map.entry("Portakal", BigDecimal.valueOf(9)),
            Map.entry("Limon", BigDecimal.valueOf(8)), Map.entry("Çilek", BigDecimal.valueOf(12)),
            Map.entry("Kiraz", BigDecimal.valueOf(15)), Map.entry("Şeftali", BigDecimal.valueOf(10)),
            Map.entry("Zeytin", BigDecimal.valueOf(12)),

            // Çiftlik (Farm) - Crops
            Map.entry("Buğday", BigDecimal.valueOf(5)), Map.entry("Mısır", BigDecimal.valueOf(6)),
            Map.entry("Arpa", BigDecimal.valueOf(5)), Map.entry("Ayçiçeği", BigDecimal.valueOf(7)),
            Map.entry("Pamuk", BigDecimal.valueOf(10)), Map.entry("Tütün", BigDecimal.valueOf(15)),
            Map.entry("Kakao", BigDecimal.valueOf(20)),
            // Farm - Animal Products
            Map.entry("Süt", BigDecimal.valueOf(15)), Map.entry("Yumurta", BigDecimal.valueOf(5)),
            Map.entry("Yün", BigDecimal.valueOf(20)), Map.entry("Deri", BigDecimal.valueOf(25)),
            // Farm - Animals (Though usually not sold in shops directly like checking algo,
            // but good to have)
            Map.entry("Tavuk", BigDecimal.valueOf(50)), Map.entry("Hindi", BigDecimal.valueOf(75)),
            Map.entry("Koyun", BigDecimal.valueOf(200)), Map.entry("Keçi", BigDecimal.valueOf(180)),
            Map.entry("İnek", BigDecimal.valueOf(500)),

            // Maden (Mine) - Raw
            Map.entry("Taş", BigDecimal.valueOf(2)), Map.entry("Kömür", BigDecimal.valueOf(10)),
            Map.entry("Demir", BigDecimal.valueOf(20)), Map.entry("Bakır", BigDecimal.valueOf(25)),
            Map.entry("Tuz", BigDecimal.valueOf(5)), Map.entry("Petrol", BigDecimal.valueOf(50)),
            Map.entry("Gümüş", BigDecimal.valueOf(100)), Map.entry("Altın", BigDecimal.valueOf(500)),

            // Fabrika (Factory) - Processed
            Map.entry("Un", BigDecimal.valueOf(10)), Map.entry("Ekmek", BigDecimal.valueOf(5)),
            Map.entry("Su", BigDecimal.valueOf(2)), Map.entry("Ayçiçek Yağı", BigDecimal.valueOf(20)),
            Map.entry("Meyve Suyu", BigDecimal.valueOf(5)), Map.entry("Peynir", BigDecimal.valueOf(20)),
            Map.entry("Yoğurt", BigDecimal.valueOf(5)), Map.entry("Sucuk", BigDecimal.valueOf(25)),
            Map.entry("Salam", BigDecimal.valueOf(20)), Map.entry("Bira", BigDecimal.valueOf(35)),
            Map.entry("Sigara", BigDecimal.valueOf(40)), Map.entry("Puro", BigDecimal.valueOf(100)),
            Map.entry("Çikolata", BigDecimal.valueOf(50)), Map.entry("Sabun", BigDecimal.valueOf(15)),
            Map.entry("Kumaş", BigDecimal.valueOf(40)), Map.entry("Kıyafet", BigDecimal.valueOf(80)),
            Map.entry("Ceket", BigDecimal.valueOf(150)), Map.entry("Ayakkabı", BigDecimal.valueOf(120)),
            Map.entry("Demir Levha", BigDecimal.valueOf(50)), Map.entry("Çelik", BigDecimal.valueOf(80)),
            Map.entry("Demir Çelik", BigDecimal.valueOf(80)), Map.entry("Alet Ekipman", BigDecimal.valueOf(150)),
            Map.entry("Plastik", BigDecimal.valueOf(10)), Map.entry("Çömlek", BigDecimal.valueOf(20)),
            Map.entry("Kuyumcu Malzemeleri", BigDecimal.valueOf(200)),

            // Jewelry Items (Shop output) check JEWELER_ITEMS
            Map.entry("Yüzük", BigDecimal.valueOf(900)), Map.entry("Küpe", BigDecimal.valueOf(500)),
            Map.entry("Kolye", BigDecimal.valueOf(1500)), Map.entry("Bilezik", BigDecimal.valueOf(750)));
}
