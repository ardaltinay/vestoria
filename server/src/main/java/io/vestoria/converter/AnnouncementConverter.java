package io.vestoria.converter;

import io.vestoria.dto.response.AnnouncementDto;
import io.vestoria.entity.AnnouncementEntity;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class AnnouncementConverter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AnnouncementDto toDto(AnnouncementEntity entity) {
        return AnnouncementDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .type(entity.getType())
                .createdTime(entity.getCreatedTime() != null ? entity.getCreatedTime().format(DATE_FORMATTER) : null)
                .isActive(entity.getIsActive())
                .build();
    }
}
