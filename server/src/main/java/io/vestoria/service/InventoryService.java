package io.vestoria.service;

import io.vestoria.entity.BuildingEntity;
import io.vestoria.entity.ItemEntity;
import io.vestoria.entity.UserEntity;
import io.vestoria.enums.BuildingSubType;
import io.vestoria.enums.BuildingType;
import io.vestoria.enums.ItemTier;
import io.vestoria.enums.ItemUnit;
import io.vestoria.exception.BadRequestException;
import io.vestoria.exception.ResourceNotFoundException;
import io.vestoria.exception.UnauthorizedAccessException;
import io.vestoria.repository.BuildingRepository;
import io.vestoria.repository.ItemRepository;
import io.vestoria.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public ItemEntity updateItemPrice(@NonNull UUID itemId, BigDecimal price, @NonNull String username) {
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
    @SuppressWarnings("null")
    public ItemEntity transferToBuilding(@NonNull UUID itemId, @NonNull UUID buildingId, Integer quantity,
            @NonNull String username) {
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
        int currentStock = building.getItems().stream().mapToInt(ItemEntity::getQuantity).sum();

        if (currentStock + quantity > building.getMaxStock()) {
            throw new BadRequestException(
                    "Depo kapasitesi yetersiz! Maksimum stok: " + building.getMaxStock() + ", Mevcut: " + currentStock);
        }

        // Validate item compatibility
        if (BuildingType.SHOP.equals(building.getType())) {
            if (building.getSubType().getMarketableProducts() == null
                    || !building.getSubType().getMarketableProducts().contains(item.getName().trim())) {
                throw new BadRequestException("Bu dükkan bu ürünü kabul etmiyor");
            }
        } else {
            // For other buildings (Factory, Farm, Mine, Garden), check if they produce this
            // item
            if (!BuildingSubType.valueOf(building.getType().name()).getProducedItemNames()
                    .contains(item.getName().trim())) {
                throw new BadRequestException("Bu işletme bu ürünü kabul etmiyor");
            }
        }

        // Check quantity
        if (quantity <= 0 || quantity > item.getQuantity()) {
            throw new BadRequestException("Geçersiz miktar");
        }

        // Check for mergeable item
        ItemEntity existingItem = building.getItems().stream().filter(i -> {
            boolean nameMatch = i.getName().trim().equalsIgnoreCase(item.getName().trim());
            boolean qualityMatch = i.getQualityScore().compareTo(item.getQualityScore()) == 0;
            return nameMatch && qualityMatch;
        }).findFirst().orElse(null);

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
            // Check if this new item adds a new product type (slot usage)
            List<ItemEntity> currentItems = building.getItems();
            long distinctProductTypeCount = currentItems.stream()
                    .map(i -> i.getName().trim().toLowerCase())
                    .distinct()
                    .count();

            // If the item we are adding is NOT already in the list (by name), we need a new
            // slot
            boolean isNewType = currentItems.stream()
                    .noneMatch(i -> i.getName().trim().equalsIgnoreCase(item.getName().trim()));

            if (isNewType && distinctProductTypeCount >= building.getMaxSlots()) {
                throw new BadRequestException(
                        "Bina slot kapasitesi dolu (" + building.getMaxSlots() + " farklı ürün çeşidi)");
            }

            // If transferring partial quantity, split the item
            if (quantity < item.getQuantity()) {
                // Reduce original item quantity
                item.setQuantity(item.getQuantity() - quantity);
                itemRepository.save(item);

                // Create new item for building
                ItemEntity newItem = Objects.requireNonNull(ItemEntity.builder().name(item.getName())
                        .unit(item.getUnit()).price(item.getPrice()).cost(item.getCost()) // Copy cost
                        .quantity(quantity).qualityScore(item.getQualityScore()).tier(item.getTier()).building(building)
                        .owner(user).build());
                return itemRepository.save(newItem);
            } else {
                // Transfer entire item
                item.setBuilding(building);
                return itemRepository.save(item);
            }
        }
    }

    @Transactional
    public void addItemToInventory(UserEntity user, String productName, int quantity, ItemUnit unit, ItemTier tier,
            BigDecimal qualityScore) {
        // Check if item already exists in centralized inventory (building is null)
        ItemEntity existingItem = itemRepository.findAllByOwner_UsernameAndBuildingIsNull(user.getUsername()).stream()
                .filter(i -> {
                    boolean nameMatch = i.getName().trim().equalsIgnoreCase(productName.trim());
                    boolean qualityMatch = i.getQualityScore().compareTo(qualityScore) == 0;
                    return nameMatch && qualityMatch;
                }).findFirst().orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            itemRepository.save(existingItem);
        } else {
            ItemEntity newItem = ItemEntity.builder().name(productName).quantity(quantity).unit(unit).tier(tier)
                    .qualityScore(qualityScore).owner(user).building(null) // Centralized inventory
                    .build();
            ItemEntity saved = itemRepository.save(newItem);
        }
    }
}
