package io.vestoria.controller;

import io.vestoria.entity.ItemEntity;
import io.vestoria.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

  private final ItemRepository itemRepository;

  @GetMapping
  public ResponseEntity<List<ItemEntity>> getMyInventory(Principal principal) {
    return ResponseEntity.ok(itemRepository.findAllByBuilding_Owner_Username(principal.getName()));
  }
}
