package io.vestoria.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarketStatsDto {
  private String itemName;
  private BigDecimal minPrice;
  private Long activeCount;
}
