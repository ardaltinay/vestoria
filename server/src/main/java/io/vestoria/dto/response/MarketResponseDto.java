package io.vestoria.dto.response;

import io.vestoria.enums.ItemTier;
import io.vestoria.enums.ItemUnit;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class MarketResponseDto implements Serializable {
    private UUID id;
    private String sellerUsername;
    private UUID itemId;
    private String itemName;
    private ItemUnit itemUnit;
    private ItemTier itemTier;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal qualityScore;
}
