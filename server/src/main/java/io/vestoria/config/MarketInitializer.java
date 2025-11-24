package io.vestoria.config;

import io.vestoria.entity.BuildingEntity;
import io.vestoria.entity.ItemEntity;
import io.vestoria.entity.MarketEntity;
import io.vestoria.entity.UserEntity;
import io.vestoria.enums.BuildingStatus;
import io.vestoria.enums.BuildingSubType;
import io.vestoria.enums.BuildingTier;
import io.vestoria.enums.BuildingType;
import io.vestoria.enums.ItemCategory;
import io.vestoria.enums.ItemTier;
import io.vestoria.enums.ItemUnit;
import io.vestoria.repository.BuildingRepository;
import io.vestoria.repository.ItemRepository;
import io.vestoria.repository.MarketRepository;
import io.vestoria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class MarketInitializer {

  private final UserRepository userRepository;
  private final BuildingRepository buildingRepository;
  private final ItemRepository itemRepository;
  private final MarketRepository marketRepository;
  private final PasswordEncoder passwordEncoder;
  private final JdbcTemplate jdbcTemplate;

  @Bean
  public CommandLineRunner initMarket() {
    return args -> {
      // Fix constraint for new Enum values (LITER, METER)
      try {
        jdbcTemplate.execute("ALTER TABLE items DROP CONSTRAINT IF EXISTS items_unit_check");
      } catch (Exception e) {
        System.out.println("Constraint drop failed or not needed: " + e.getMessage());
      }

      // 1. Create Bot User
      if (userRepository.findByUsername("market_bot").isPresent()) {
        return; // Already initialized
      }

      UserEntity botUser = UserEntity.builder()
          .username("market_bot")
          .password(passwordEncoder.encode("bot_secret_password_123"))
          .email("bot@vestoria.io")
          .balance(BigDecimal.valueOf(1_000_000_000)) // Rich bot
          .level(100)
          .xp(0L)
          .isAdmin(false)
          .build();
      userRepository.save(botUser);

      // 2. Create Bot Warehouse (Factory)
      BuildingEntity warehouse = BuildingEntity.builder()
          .owner(botUser)
          .type(BuildingType.FACTORY)
          .tier(BuildingTier.LARGE)
          .subType(BuildingSubType.GENERIC)
          .level(3)
          .productionRate(BigDecimal.ZERO)
          .maxSlots(100)
          .status(BuildingStatus.ACTIVE)
          .cost(BigDecimal.ZERO)
          .maxStock(100000)
          .build();
      buildingRepository.save(warehouse);

      // 3. Seed Items and Listings
      seedItems(warehouse, botUser);
    };
  }

  private void seedItems(BuildingEntity warehouse, UserEntity botUser) {
    List<SeedItem> items = Arrays.asList(
        // Market Items (Food)
        new SeedItem("Ekmek", ItemUnit.PIECE, 5.0, ItemCategory.FOOD, ItemTier.LOW),
        new SeedItem("Su", ItemUnit.LITER, 2.0, ItemCategory.FOOD, ItemTier.LOW),
        new SeedItem("Peynir", ItemUnit.KG, 150.0, ItemCategory.FOOD, ItemTier.LOW),

        // Greengrocer Items (Fresh Produce)
        new SeedItem("Domates", ItemUnit.KG, 25.0, ItemCategory.FRESH_PRODUCE, ItemTier.LOW),
        new SeedItem("Elma", ItemUnit.KG, 30.0, ItemCategory.FRESH_PRODUCE, ItemTier.LOW),
        new SeedItem("Patates", ItemUnit.KG, 15.0, ItemCategory.FRESH_PRODUCE, ItemTier.LOW),

        // Clothing Items
        new SeedItem("Tişört", ItemUnit.PIECE, 250.0, ItemCategory.CLOTHING, ItemTier.LOW),
        new SeedItem("Pantolon", ItemUnit.PIECE, 400.0, ItemCategory.CLOTHING, ItemTier.LOW),
        new SeedItem("Ceket", ItemUnit.PIECE, 800.0, ItemCategory.CLOTHING, ItemTier.MEDIUM),

        // Jewelry Items
        new SeedItem("Altın Kolye", ItemUnit.PIECE, 5000.0, ItemCategory.JEWELRY, ItemTier.SCARCE),
        new SeedItem("Gümüş Yüzük", ItemUnit.PIECE, 1500.0, ItemCategory.JEWELRY, ItemTier.SCARCE),

        // Industrial / Raw Materials
        new SeedItem("Demir", ItemUnit.KG, 50.0, ItemCategory.RAW_MATERIAL, ItemTier.LOW),
        new SeedItem("Çelik", ItemUnit.KG, 120.0, ItemCategory.INDUSTRIAL, ItemTier.LOW),
        new SeedItem("Kumaş", ItemUnit.METER, 80.0, ItemCategory.RAW_MATERIAL, ItemTier.LOW),
        new SeedItem("Odun", ItemUnit.KG, 10.0, ItemCategory.RAW_MATERIAL, ItemTier.LOW));

    for (SeedItem seed : items) {
      // Create Item in Warehouse
      ItemEntity item = ItemEntity.builder()
          .name(seed.name)
          .unit(seed.unit)
          .price(BigDecimal.valueOf(seed.price))
          .quantity(10000) // Huge stock
          .qualityScore(BigDecimal.valueOf(100.0)) // Perfect quality
          .tier(seed.tier)
          .category(seed.category)
          .building(warehouse)
          .supply(10000L)
          .demand(0L)
          .build();
      itemRepository.save(item);

      // Create Market Listing
      MarketEntity listing = MarketEntity.builder()
          .seller(botUser)
          .item(item)
          .price(BigDecimal.valueOf(seed.price)) // Sell at base price
          .quantity(5000) // List half of stock
          .isActive(true)
          .build();
      marketRepository.save(listing);
    }
  }

  @Value
  private static class SeedItem {
    String name;
    ItemUnit unit;
    double price;
    ItemCategory category;
    ItemTier tier;
  }
}
