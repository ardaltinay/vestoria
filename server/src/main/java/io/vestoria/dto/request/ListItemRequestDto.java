package io.vestoria.dto.request;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

@Data
public class ListItemRequestDto implements Serializable {
    private UUID itemId;
    private Integer quantity;
    private BigDecimal price;
}
