package io.vestoria.dto.request;

import java.io.Serializable;
import java.util.UUID;
import lombok.Data;

@Data
public class BuyItemRequestDto implements Serializable {
    private UUID marketItemId;
    private Integer quantity;
}
