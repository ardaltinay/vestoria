package io.vestoria.config;

import io.vestoria.constant.Constants;
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
                new SeedItem("Ekmek", ItemUnit.PIECE, ItemTier.LOW),
                new SeedItem("Su", ItemUnit.LITER, ItemTier.LOW),
                new SeedItem("Peynir", ItemUnit.KG, ItemTier.MEDIUM),

                // Greengrocer Items (Fresh Produce)
                new SeedItem("Domates", ItemUnit.KG, ItemTier.LOW),
                new SeedItem("Elma", ItemUnit.KG, ItemTier.LOW),
                new SeedItem("Patates", ItemUnit.KG, ItemTier.LOW),

                // Clothing Items
                new SeedItem("Kumaş", ItemUnit.PIECE, ItemTier.LOW),
                new SeedItem("Ayakkabı", ItemUnit.PIECE, ItemTier.HIGH),
                new SeedItem("Ceket", ItemUnit.PIECE, ItemTier.HIGH),

                // Jewelry Items
                new SeedItem("Kolye", ItemUnit.PIECE, ItemTier.SCARCE),
                new SeedItem("Yüzük", ItemUnit.PIECE, ItemTier.SCARCE),

                // Industrial / Raw Materials
                new SeedItem("Demir", ItemUnit.KG, ItemTier.MEDIUM),
                new SeedItem("Çelik", ItemUnit.KG, ItemTier.MEDIUM));

        for (SeedItem seed : items) {
            // Get Base Price and apply 25% markup (Ceiling Price Strategy)
            BigDecimal basePrice = Constants.BASE_PRICES.getOrDefault(seed.name,
                    BigDecimal.valueOf(10));
            BigDecimal ceilingPrice = basePrice.multiply(BigDecimal.valueOf(1.25));

            // Create Item in Warehouse
            ItemEntity item = ItemEntity.builder().name(seed.name).unit(seed.unit).price(ceilingPrice)
                    .quantity(10000) // Huge stock
                    .qualityScore(BigDecimal.valueOf(50.0)).tier(seed.tier).building(warehouse).owner(botUser).build();
            itemRepository.save(item);

            // Create Market Listing
            MarketEntity listing = MarketEntity.builder().seller(botUser).item(item)
                    .price(ceilingPrice).quantity(item.getQuantity()).isActive(true).build();
            marketRepository.save(listing);
        }
    }

    @Value
    private static class SeedItem {
        String name;
        ItemUnit unit;
        ItemTier tier;
    }
}
