package io.vestoria.dto.request;

import lombok.Data;

@Data
public class WithdrawRequestDto {
    private String productId;
    private Integer quantity;
}
