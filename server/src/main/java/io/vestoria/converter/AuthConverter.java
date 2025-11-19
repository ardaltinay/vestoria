package io.vestoria.converter;

import io.vestoria.dto.response.AuthResponseDto;
import io.vestoria.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class AuthConverter {

    public AuthResponseDto toAuthDto(UserEntity entity) {
        return AuthResponseDto.builder()
                .id(entity.getId().toString())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .build();
    }
}
