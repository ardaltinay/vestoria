package io.vestoria.controller;

import java.security.Principal;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.vestoria.converter.MarketConverter;
import io.vestoria.dto.request.BuyItemRequestDto;
import io.vestoria.dto.request.ListItemRequestDto;
import io.vestoria.dto.response.MarketResponseDto;
import io.vestoria.service.MarketService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class MarketController {

  private final MarketService marketService;
  private final MarketConverter marketConverter;

  @GetMapping("/listings")
  public ResponseEntity<Page<MarketResponseDto>> getActiveListings(
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size) {

    return ResponseEntity.ok(marketService.getActiveListings(search, page, size));
  }

  @PostMapping("/list/{itemId}")
  public ResponseEntity<MarketResponseDto> listItem(@PathVariable UUID itemId, @RequestBody ListItemRequestDto request,
      Principal principal) {
    return ResponseEntity
        .ok(marketConverter.toResponseDto(marketService.listItem(principal.getName(), itemId, request)));
  }

  @PostMapping("/buy/{marketItemId}")
  public ResponseEntity<Void> buyItem(@PathVariable UUID marketItemId, @RequestBody BuyItemRequestDto request,
      Principal principal) {
    marketService.buyItem(principal.getName(), marketItemId, request);
    return ResponseEntity.ok().build();
  }
}
