package io.vestoria.service;

import io.vestoria.converter.AnnouncementConverter;
import io.vestoria.dto.response.AnnouncementDto;
import io.vestoria.entity.AnnouncementEntity;
import io.vestoria.enums.AnnouncementType;
import io.vestoria.repository.AnnouncementRepository;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnnouncementService {
    private final AnnouncementRepository announcementRepository;
    private final AnnouncementConverter converter;

    @Cacheable("announcements")
    public List<AnnouncementDto> getActiveAnnouncements() {
        return announcementRepository.findAllByIsActiveTrueOrderByCreatedTimeDesc()
                .stream()
                .map(converter::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "announcements", allEntries = true)
    public AnnouncementDto createAnnouncement(String title, String content, AnnouncementType type) {
        AnnouncementEntity announcement = AnnouncementEntity.builder()
                .title(title)
                .content(content)
                .type(type)
                .isActive(true)
                .build();
        return converter.toDto(announcementRepository.save(announcement));
    }
}
