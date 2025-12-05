package io.vestoria.converter;

import io.vestoria.dto.response.NotificationDto;
import io.vestoria.entity.NotificationEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificationConverter {

    public NotificationDto toDto(NotificationEntity entity) {
        return NotificationDto.builder().id(entity.getId()).message(entity.getMessage()).isRead(entity.isRead())
                .createdAt(entity.getCreatedTime()).build();
    }
}
