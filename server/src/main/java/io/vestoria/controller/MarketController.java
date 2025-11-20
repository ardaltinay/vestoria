package io.vestoria.controller;

import io.vestoria.dto.request.BuyItemRequestDto;
import io.vestoria.dto.request.ListItemRequestDto;
import io.vestoria.service.MarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import io.vestoria.entity.MarketEntity;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class MarketController {

  private final MarketService marketService;

  @GetMapping("/listings")
  public ResponseEntity<List<MarketEntity>> getActiveListings() {
    return ResponseEntity.ok(marketService.getActiveListings());
  }

  @PostMapping("/list/{itemId}")
  public ResponseEntity<MarketEntity> listItem(@PathVariable UUID itemId, @RequestBody ListItemRequestDto request,
      Principal principal) {
    return ResponseEntity.ok(marketService.listItem(principal.getName(), itemId, request));
  }

  @PostMapping("/buy/{marketItemId}")
  public ResponseEntity<Void> buyItem(@PathVariable UUID marketItemId, @RequestBody BuyItemRequestDto request,
      Principal principal) {
    marketService.buyItem(principal.getName(), marketItemId, request);
    return ResponseEntity.ok().build();
  }
}
