package io.vestoria.dto.request;

import lombok.Data;
import java.math.BigDecimal;

import java.util.UUID;

@Data
public class ListItemRequestDto {
  private UUID itemId;
  private Integer quantity;
  private BigDecimal price;
}
