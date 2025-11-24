package io.vestoria.service;

import io.vestoria.entity.AnnouncementEntity;
import io.vestoria.enums.AnnouncementType;
import io.vestoria.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

  private final AnnouncementRepository announcementRepository;

  @Cacheable("announcements")
  public List<AnnouncementEntity> getActiveAnnouncements() {
    return announcementRepository.findAllByIsActiveTrueOrderByCreatedTimeDesc();
  }

  @Transactional
  @SuppressWarnings("null")
  @CacheEvict(value = "announcements", allEntries = true)
  public AnnouncementEntity createAnnouncement(String title, String content, AnnouncementType type) {
    AnnouncementEntity announcement = AnnouncementEntity.builder()
        .title(title)
        .content(content)
        .type(type)
        .isActive(true)
        .build();
    return announcementRepository.save(announcement);
  }
}
