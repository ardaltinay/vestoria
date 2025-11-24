package io.vestoria.dto.request;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class BuyItemRequestDto implements Serializable {
  private UUID marketItemId;
  private Integer quantity;
}
