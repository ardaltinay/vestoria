package io.vestoria.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AuthResponseDto(
    String id,
    String username,
    String email,
    BigDecimal balance,
    Integer level,
    Long xp,
    Boolean isAdmin) {
}
