package io.vestoria.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarketStatsDto {
    private String itemName;
    private BigDecimal minPrice;
    private Long activeCount;
}
