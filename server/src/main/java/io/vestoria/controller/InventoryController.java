package io.vestoria.controller;

import io.vestoria.entity.ItemEntity;
import io.vestoria.repository.ItemRepository;
import io.vestoria.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

  private final InventoryService inventoryService;

  @GetMapping
  public ResponseEntity<List<ItemEntity>> getMyInventory(Principal principal) {
    return ResponseEntity.ok(inventoryService.getMyInventory(principal.getName()));
  }

  @PutMapping("/item/{itemId}/price")
  public ResponseEntity<?> updateItemPrice(@PathVariable UUID itemId,
      @RequestBody Map<String, BigDecimal> request,
      Principal principal) {
    return ResponseEntity.ok(inventoryService.updateItemPrice(itemId, request.get("price"), principal.getName()));
  }
}
