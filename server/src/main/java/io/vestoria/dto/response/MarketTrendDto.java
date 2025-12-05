package io.vestoria.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketTrendDto implements Serializable {
    private String name;
    private BigDecimal price;
    private String trend; // "up", "down", "stable"
    private Double change; // Percentage change
}
