package io.vestoria.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record AuthResponseDto(String id, String username, String email, BigDecimal balance, Integer level, Long xp,
        LocalDateTime createdTime, Boolean isAdmin) {
}
