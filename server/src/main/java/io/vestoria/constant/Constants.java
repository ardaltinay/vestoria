package io.vestoria.constant;

import java.util.List;
import java.util.Map;

public class Constants {
        // Bahçe Ürünleri
        public static final List<String> GARDEN_ITEMS = List.of(
                        "Zeytin", "Havuç", "Patates", "Domates", "Salatalık", "Biber", "Patlıcan", "Soğan",
                        "Elma", "Armut", "Mandalin", "Portakal", "Çilek", "Kiraz", "Şeftali", "Limon", "Karpuz",
                        "Kavun");

        // Çiftlik Ürünleri
        public static final List<String> FARM_ITEMS = List.of(
                        "İnek", "Koyun", "Keçi", "Deri", "Süt", "Yumurta", "Tavuk", "Hindi", "Yün",
                        "Pamuk", "Buğday", "Mısır", "Arpa", "Tütün", "Ayçiçek", "Kakao");

        // Maden Ürünleri
        public static final List<String> MINE_ITEMS = List.of(
                        "Kömür", "Demir", "Bakır", "Altın", "Gümüş", "Petrol", "Taş", "Tuz");

        // Fabrika Ürünleri ve Gereksinimleri (Ürün -> Hammaddeler)
        public static final Map<String, List<String>> FACTORY_MAP = Map.ofEntries(
                        // Süt Ürünleri
                        Map.entry("Peynir", List.of("Süt")),
                        Map.entry("Yoğurt", List.of("Süt")),

                        // Tahıl Ürünleri
                        Map.entry("Ekmek", List.of("Un")),
                        Map.entry("Un", List.of("Buğday")),

                        // İçecekler
                        Map.entry("Meyve Suyu", List.of("Meyve")),
                        Map.entry("Bira", List.of("Arpa")),

                        // Et Ürünleri
                        Map.entry("Sucuk", List.of("Et")),
                        Map.entry("Salam", List.of("Et")),

                        // Tekstil Ürünleri
                        Map.entry("Kumaş", List.of("Yün")),
                        Map.entry("Kıyafet", List.of("Pamuk")),
                        Map.entry("Ceket", List.of("Deri", "Pamuk")),
                        Map.entry("Ayakkabı", List.of("Deri")),

                        // Metal Ürünleri
                        Map.entry("Demir Levha", List.of("Demir")),
                        Map.entry("Alet Ekipman", List.of("Çelik")),
                        Map.entry("Demir Çelik", List.of("Kömür", "Demir")),

                        // Kimyasal Ürünler
                        Map.entry("Plastik", List.of("Petrol")),
                        Map.entry("Sabun", List.of("Bitkisel Yağ")),

                        // Tatlı ve Atıştırmalıklar
                        Map.entry("Çikolata", List.of("Kakao", "Süt")),

                        // Kuyumcu Ürünleri
                        Map.entry("Kuyumcu Malzemeleri", List.of("Altın", "Gümüş")),

                        // Seramik
                        Map.entry("Çömlek", List.of("Bakır")),

                        // Tütün Ürünleri
                        Map.entry("Sigara", List.of("Tütün")),
                        Map.entry("Puro", List.of("Tütün")));

        // Market Ürünleri
        public static final List<String> MARKET_ITEMS = List.of(
                        "Tuz", "Sigara", "Puro", "Ekmek", "Meyve Suyu", "Peynir", "Yoğurt", "Zeytin",
                        "Un", "Sucuk", "Salam", "Sabun", "Çikolata", "Bira", "Çömlek");

        // Manav Ürünleri
        public static final List<String> GREENGROCER_ITEMS = List.of(
                        "Havuç", "Patates", "Domates", "Salatalık", "Biber", "Patlıcan", "Soğan",
                        "Elma", "Armut", "Mandalin", "Portakal", "Çilek", "Kiraz", "Şeftali", "Limon", "Karpuz",
                        "Kavun");

        // Giyim Mağazası Ürünleri
        public static final List<String> CLOTHING_ITEMS = List.of(
                        "Kumaş", "Kıyafet", "Ceket", "Ayakkabı");

        // Kuyumcu Ürünleri
        public static final List<String> JEWELER_ITEMS = List.of(
                        "Bilezik", "Kolye", "Küpe", "Yüzük");
}
