package io.vestoria.service;

import io.vestoria.constant.Constants;
import io.vestoria.entity.BuildingEntity;
import io.vestoria.entity.ItemEntity;
import io.vestoria.entity.UserEntity;
import io.vestoria.enums.BuildingType;
import io.vestoria.exception.BadRequestException;
import io.vestoria.exception.ResourceNotFoundException;
import io.vestoria.exception.UnauthorizedAccessException;
import io.vestoria.repository.BuildingRepository;
import io.vestoria.repository.ItemRepository;
import io.vestoria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

  private final ItemRepository itemRepository;
  private final UserRepository userRepository;
  private final BuildingRepository buildingRepository;

  /**
   * Get all inventory items for a user (including centralized inventory)
   */
  @Transactional(readOnly = true)
  public List<ItemEntity> getMyInventory(String username) {
    return itemRepository.findAllByOwner_Username(username);
  }

  /**
   * Get only centralized inventory (items not assigned to any building)
   */
  @Transactional(readOnly = true)
  public List<ItemEntity> getCentralizedInventory(String username) {
    return itemRepository.findAllByOwner_UsernameAndBuildingIsNull(username);
  }

  @Transactional
  public ItemEntity updateItemPrice(UUID itemId, BigDecimal price, String username) {
    ItemEntity item = itemRepository.findById(itemId)
        .orElseThrow(() -> new ResourceNotFoundException("Ürün bulunamadı"));

    UserEntity user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

    // Check ownership
    if (!item.getOwner().getId().equals(user.getId())) {
      throw new UnauthorizedAccessException("Bu ürün size ait değil");
    }

    item.setPrice(price);
    return itemRepository.save(item);
  }

  /**
   * Transfer item from centralized inventory to a building
   */
  @Transactional
  public ItemEntity transferToBuilding(UUID itemId, UUID buildingId, Integer quantity, String username) {
    ItemEntity item = itemRepository.findById(itemId)
        .orElseThrow(() -> new ResourceNotFoundException("Ürün bulunamadı"));

    UserEntity user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

    BuildingEntity building = buildingRepository.findById(buildingId)
        .orElseThrow(() -> new ResourceNotFoundException("Bina bulunamadı"));

    // Validate ownership
    if (!item.getOwner().getId().equals(user.getId())) {
      throw new UnauthorizedAccessException("Bu ürün size ait değil");
    }

    if (!building.getOwner().getId().equals(user.getId())) {
      throw new UnauthorizedAccessException("Bu bina size ait değil");
    }

    // Check building storage capacity (Total Quantity)
    int currentStock = building.getItems().stream()
        .mapToInt(ItemEntity::getQuantity)
        .sum();

    if (currentStock + quantity > building.getMaxStock()) {
      throw new BadRequestException(
          "Depo kapasitesi yetersiz! Maksimum stok: " + building.getMaxStock() + ", Mevcut: " + currentStock);
    }

    // Validate item compatibility
    if (BuildingType.SHOP.equals(building.getType())) {
      if (building.getSubType().getMarketableProducts() == null ||
          !building.getSubType().getMarketableProducts().contains(item.getName().trim())) {
        throw new BadRequestException("Bu dükkan bu ürünü kabul etmiyor");
      }
    } else if (BuildingType.FACTORY.equals(building.getType())) {
      boolean isRawMaterial = Constants.FACTORY_MAP.values().stream()
          .flatMap(java.util.List::stream)
          .anyMatch(ingredient -> ingredient.equals(item.getName().trim()));

      if (!isRawMaterial) {
        throw new BadRequestException("Bu fabrika bu ürünü hammadde olarak kullanmıyor");
      }
    } else {
      throw new BadRequestException("Bu bina ürün kabul etmiyor");
    }

    // Check quantity
    if (quantity <= 0 || quantity > item.getQuantity()) {
      throw new BadRequestException("Geçersiz miktar");
    }

    // Check for mergeable item
    ItemEntity existingItem = building.getItems().stream()
        .filter(i -> {
          boolean nameMatch = i.getName().trim().equalsIgnoreCase(item.getName().trim());
          boolean qualityMatch = i.getQualityScore().compareTo(item.getQualityScore()) == 0;
          return nameMatch && qualityMatch;
        })
        .findFirst()
        .orElse(null);

    if (existingItem != null) {
      // Merge into existing item
      existingItem.setQuantity(existingItem.getQuantity() + quantity);
      ItemEntity savedExisting = itemRepository.save(existingItem);

      if (quantity < item.getQuantity()) {
        // Partial transfer
        item.setQuantity(item.getQuantity() - quantity);
        itemRepository.save(item);
      } else {
        // Full transfer - delete source item
        itemRepository.delete(item);
      }
      return savedExisting;
    } else {
      // Check building capacity (Slots) - Only if we are creating a new item
      long currentItemCount = itemRepository.countByBuilding_Id(buildingId);
      if (currentItemCount >= building.getMaxSlots()) {
        throw new BadRequestException("Bina slot kapasitesi dolu (" + building.getMaxSlots() + " slot)");
      }

      // If transferring partial quantity, split the item
      if (quantity < item.getQuantity()) {
        // Reduce original item quantity
        item.setQuantity(item.getQuantity() - quantity);
        itemRepository.save(item);

        // Create new item for building
        ItemEntity newItem = ItemEntity.builder()
            .name(item.getName())
            .unit(item.getUnit())
            .price(item.getPrice())
            .cost(item.getCost()) // Copy cost
            .quantity(quantity)
            .qualityScore(item.getQualityScore())
            .tier(item.getTier())
            .building(building)
            .owner(user)
            .build();
        return itemRepository.save(newItem);
      } else {
        // Transfer entire item
        item.setBuilding(building);
        return itemRepository.save(item);
      }
    }
  }
}
