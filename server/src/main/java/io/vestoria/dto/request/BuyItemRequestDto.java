package io.vestoria.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class BuyItemRequestDto {
  private UUID marketItemId;
  private Integer quantity;
}
