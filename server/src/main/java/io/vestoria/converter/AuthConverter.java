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
                .balance(entity.getBalance())
                .level(entity.getLevel())
                .createdTime(entity.getCreatedTime())
                .xp(entity.getXp())
                .isAdmin(entity.getIsAdmin())
                .build();
    }

    public AuthResponseDto toResponseDto(UserEntity entity) {
        return toAuthDto(entity);
    }
}
