package io.vestoria.service;

import io.vestoria.entity.ItemEntity;
import io.vestoria.entity.UserEntity;
import io.vestoria.exception.ResourceNotFoundException;
import io.vestoria.exception.UnauthorizedAccessException;
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

  @Transactional(readOnly = true)
  public List<ItemEntity> getMyInventory(String username) {
    return itemRepository.findAllByBuilding_Owner_Username(username);
  }

  @Transactional
  public ItemEntity updateItemPrice(UUID itemId, BigDecimal price, String username) {
    ItemEntity item = itemRepository.findById(itemId)
        .orElseThrow(() -> new ResourceNotFoundException("Ürün bulunamadı"));

    UserEntity user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

    if (!item.getBuilding().getOwner().getId().equals(user.getId())) {
      throw new UnauthorizedAccessException("Bu ürün size ait değil");
    }

    item.setPrice(price);
    return itemRepository.save(item);
  }
}
