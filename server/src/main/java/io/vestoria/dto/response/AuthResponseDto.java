package io.vestoria.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record AuthResponseDto(String id, String username, String email, BigDecimal balance, Integer level, Long xp,
                              LocalDateTime createdTime, Boolean isAdmin) {
}
