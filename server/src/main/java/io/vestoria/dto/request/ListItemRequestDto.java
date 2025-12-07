package io.vestoria.dto.request;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ListItemRequestDto implements Serializable {
    private UUID itemId;
    private Integer quantity;
    private BigDecimal price;
}
