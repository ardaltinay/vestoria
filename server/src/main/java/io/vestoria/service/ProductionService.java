package io.vestoria.service;

import io.vestoria.entity.BuildingEntity;
import io.vestoria.entity.ItemEntity;
import io.vestoria.entity.UserEntity;
import io.vestoria.enums.BuildingType;
import io.vestoria.enums.ItemTier;
import io.vestoria.enums.ItemUnit;
import io.vestoria.repository.BuildingRepository;
import io.vestoria.repository.ItemRepository;
import io.vestoria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import io.vestoria.enums.ItemCategory;

@Service
@RequiredArgsConstructor
public class ProductionService {

  private final BuildingRepository buildingRepository;
  private final ItemRepository itemRepository;
  private final UserRepository userRepository;

  @Scheduled(fixedRate = 60000) // Every minute
  @Transactional
  public void produceItems() {
    List<BuildingEntity> buildings = buildingRepository.findAll();

    for (BuildingEntity building : buildings) {
      if (!"ACTIVE".equals(building.getStatus()))
        continue;
      if (building.getType() == BuildingType.SHOP)
        continue; // Shops don't produce

      String itemName = getItemNameForBuilding(building);
      if (itemName == null)
        continue;

      Optional<ItemEntity> existingItem = building.getItems().stream()
          .filter(i -> i.getName().equals(itemName))
          .findFirst();

      // Factory Production Logic (Requires Raw Materials)
      if (building.getType() == BuildingType.FACTORY) {
        if (!consumeRawMaterials(building, itemName)) {
          continue; // Skip production if materials are missing
        }
      }

      if (existingItem.isPresent()) {
        ItemEntity item = existingItem.get();
        item.setQuantity(item.getQuantity() + building.getProductionRate().intValue());
        itemRepository.save(item);
      } else {
        ItemEntity newItem = ItemEntity.builder()
            .name(itemName)
            .unit(ItemUnit.PIECE) // Default
            .price(BigDecimal.TEN) // Default base price
            .quantity(building.getProductionRate().intValue())
            .qualityScore(BigDecimal.ONE)
            .tier(ItemTier.LOW)
            .building(building)
            .category(getCategoryForBuildingType(building.getType()))
            .build();
        @SuppressWarnings({ "null", "unused" })
        ItemEntity saved = itemRepository.save(newItem);
      }

      // Award XP to owner
      UserEntity owner = building.getOwner();
      if (owner != null) {
        long xpGain = 10; // 10 XP per production cycle
        owner.setXp((owner.getXp() == null ? 0 : owner.getXp()) + xpGain);

        // Check Level Up
        // Simple formula: Level N requires N * 1000 XP total?
        // Or next level requires currentLevel * 1000.
        // Let's use: Level = 1 + (XP / 1000)
        int currentLevel = owner.getLevel() == null ? 1 : owner.getLevel();
        long requiredXpForNext = currentLevel * 1000L;

        if (owner.getXp() >= requiredXpForNext) {
          owner.setLevel(currentLevel + 1);
          // Maybe send notification?
        }
        // We need to save owner. Since it's attached to building, saving building might
        // cascade?
        // But we are not saving building here.
        // We should inject UserRepository and save owner, or just save owner if we can
        // access repository.
        // We need to inject UserRepository.
        userRepository.save(owner);
      }
    }
  }

  private String getItemNameForBuilding(BuildingEntity building) {
    if (building.getSubType() != null && building.getSubType().getProducedItemName() != null) {
      return building.getSubType().getProducedItemName();
    }
    return getDefaultItemForType(building.getType());
  }

  private String getDefaultItemForType(BuildingType type) {
    switch (type) {
      case GARDEN:
        return "Elma";
      case FARM:
        return "Buğday";
      case FACTORY:
        return "Kumaş";
      case MINE:
        return "Demir";
      default:
        return null;
    }
  }

  private boolean consumeRawMaterials(BuildingEntity building, String productName) {
    // Simple recipe logic
    String requiredMaterial = null;
    int requiredAmount = 1; // Default requirement

    if ("Ceket".equals(productName)) {
      requiredMaterial = "Kumaş"; // Jacket needs Fabric
      requiredAmount = 2;
    } else if ("Kumaş".equals(productName)) {
      requiredMaterial = "Pamuk"; // Fabric needs Cotton
      requiredAmount = 3;
    } else if ("Çelik".equals(productName)) {
      requiredMaterial = "Demir"; // Steel needs Iron
      requiredAmount = 2;
    } else {
      return true; // No materials needed for other products or generic ones
    }

    // Check inventory
    String finalRequiredMaterial = requiredMaterial;
    Optional<ItemEntity> materialOpt = building.getItems().stream()
        .filter(i -> i.getName().equals(finalRequiredMaterial))
        .findFirst();

    if (materialOpt.isPresent() && materialOpt.get().getQuantity() >= requiredAmount) {
      ItemEntity material = materialOpt.get();
      material.setQuantity(material.getQuantity() - requiredAmount);
      if (material.getQuantity() == 0) {
        // Optional: Remove item or keep with 0 quantity.
        // Keeping with 0 is safer for now to avoid concurrent modification if iterating
        // But here we are just modifying.
      }
      itemRepository.save(material);
      return true;
    }

    return false;
  }

  private ItemCategory getCategoryForBuildingType(BuildingType type) {
    switch (type) {
      case GARDEN:
        return ItemCategory.FRESH_PRODUCE;
      case FARM:
        return ItemCategory.RAW_MATERIAL; // Or FOOD depending on subtype
      case FACTORY:
        return ItemCategory.INDUSTRIAL; // Or CLOTHING
      case MINE:
        return ItemCategory.RAW_MATERIAL;
      default:
        return null;
    }
  }
}
