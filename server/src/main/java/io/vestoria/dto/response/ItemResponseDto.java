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
public class ItemResponseDto implements Serializable {
    private UUID id;
    private String name;
    private ItemUnit unit;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal qualityScore;
    private ItemTier tier;
}
