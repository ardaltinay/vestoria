package io.vestoria.controller;

import io.vestoria.entity.AnnouncementEntity;
import io.vestoria.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

  private final AnnouncementService announcementService;

  @GetMapping
  public ResponseEntity<List<AnnouncementEntity>> getAnnouncements() {
    return ResponseEntity.ok(announcementService.getActiveAnnouncements());
  }
}
