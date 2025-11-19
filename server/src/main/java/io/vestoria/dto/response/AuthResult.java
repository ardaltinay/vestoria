package io.vestoria.dto.response;

import lombok.Builder;

@Builder
public record AuthResult(String token, AuthResponseDto user) {
}
