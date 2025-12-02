package io.vestoria.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketUpdateDto {
  private String type; // "LIST" or "BUY"
  private UUID id;
  private String itemName;
  private int quantity;
  private BigDecimal price;
  private String sellerName;
}
