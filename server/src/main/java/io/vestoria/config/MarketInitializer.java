package io.vestoria.config;

import io.vestoria.entity.BuildingEntity;
import io.vestoria.entity.ItemEntity;
import io.vestoria.entity.MarketEntity;
import io.vestoria.entity.UserEntity;
import io.vestoria.enums.BuildingStatus;
import io.vestoria.enums.BuildingSubType;
import io.vestoria.enums.BuildingTier;
import io.vestoria.enums.BuildingType;
import io.vestoria.enums.ItemTier;
import io.vestoria.enums.ItemUnit;
import io.vestoria.repository.BuildingRepository;
import io.vestoria.repository.ItemRepository;
import io.vestoria.repository.MarketRepository;
import io.vestoria.repository.UserRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class MarketInitializer {

    private final UserRepository userRepository;
    private final BuildingRepository buildingRepository;
    private final ItemRepository itemRepository;
    private final MarketRepository marketRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initMarket() {
        return args -> {
            // 1. Create Bot User
            if (userRepository.findByUsername("vestoria").isPresent()) {
                return; // Already initialized
            }

            UserEntity botUser = UserEntity.builder().username("vestoria")
                    .password(passwordEncoder.encode("vestoria_123")).email("bot@vestoria.io")
                    .balance(BigDecimal.valueOf(900_000_000)) // Rich bot
                    .level(100).xp(0L).isAdmin(false).build();
            userRepository.save(botUser);

            // 2. Create Bot Warehouse (Factory)
            BuildingEntity warehouse = BuildingEntity.builder().name("Vestoria Building").owner(botUser)
                    .type(BuildingType.FACTORY).tier(BuildingTier.LARGE).subType(BuildingSubType.GENERIC)
                    .productionRate(BigDecimal.ZERO).maxSlots(10000).status(BuildingStatus.ACTIVE).cost(BigDecimal.ZERO)
                    .maxStock(100000).build();
            buildingRepository.save(warehouse);

            // 3. Seed Items and Listings
            seedItems(warehouse, botUser);
        };
    }

    private void seedItems(BuildingEntity warehouse, UserEntity botUser) {
        List<SeedItem> items = Arrays.asList(
                // Market Items (Food)
                new SeedItem("Ekmek", ItemUnit.PIECE, 5.0, ItemTier.LOW),
                new SeedItem("Su", ItemUnit.LITER, 2.0, ItemTier.LOW),
                new SeedItem("Peynir", ItemUnit.KG, 50.0, ItemTier.MEDIUM),

                // Greengrocer Items (Fresh Produce)
                new SeedItem("Domates", ItemUnit.KG, 10.0, ItemTier.LOW),
                new SeedItem("Elma", ItemUnit.KG, 15.0, ItemTier.LOW),
                new SeedItem("Patates", ItemUnit.KG, 8.0, ItemTier.LOW),

                // Clothing Items
                new SeedItem("Kumaş", ItemUnit.PIECE, 25.0, ItemTier.LOW),
                new SeedItem("Ayakkabı  ", ItemUnit.PIECE, 150.0, ItemTier.HIGH),
                new SeedItem("Ceket", ItemUnit.PIECE, 200.0, ItemTier.HIGH),

                // Jewelry Items
                new SeedItem("Altın Kolye", ItemUnit.PIECE, 800.0, ItemTier.SCARCE),
                new SeedItem("Gümüş Yüzük", ItemUnit.PIECE, 250.0, ItemTier.SCARCE),

                // Industrial / Raw Materials
                new SeedItem("Demir", ItemUnit.KG, 50.0, ItemTier.MEDIUM),
                new SeedItem("Çelik", ItemUnit.KG, 120.0, ItemTier.MEDIUM));

        for (SeedItem seed : items) {
            // Create Item in Warehouse
            ItemEntity item = ItemEntity.builder().name(seed.name).unit(seed.unit).price(BigDecimal.valueOf(seed.price))
                    .quantity(10000) // Huge stock
                    .qualityScore(BigDecimal.valueOf(50.0)).tier(seed.tier).building(warehouse).owner(botUser).build();
            @SuppressWarnings({"null", "unused"})
            ItemEntity savedItem = itemRepository.save(item);

            // Create Market Listing
            MarketEntity listing = MarketEntity.builder().seller(botUser).item(item)
                    .price(BigDecimal.valueOf(seed.price)).quantity(item.getQuantity()).isActive(true).build();
            @SuppressWarnings({"null", "unused"})
            MarketEntity savedListing = marketRepository.save(listing);
        }
    }

    @Value
    private static class SeedItem {
        String name;
        ItemUnit unit;
        double price;
        ItemTier tier;
    }
}
