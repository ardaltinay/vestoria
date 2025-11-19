package io.vestoria.dto.response;

import lombok.Builder;

@Builder
public record AuthResponseDto(String id, String username, String email) {
}
