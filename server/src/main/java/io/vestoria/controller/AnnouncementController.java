package io.vestoria.controller;

import io.vestoria.dto.request.CreateAnnouncementRequestDto;
import io.vestoria.entity.AnnouncementEntity;
import io.vestoria.entity.UserEntity;
import io.vestoria.exception.ResourceNotFoundException;
import io.vestoria.repository.UserRepository;
import io.vestoria.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

  private final AnnouncementService announcementService;

  private final UserRepository userRepository;

  @GetMapping
  public ResponseEntity<List<AnnouncementEntity>> getAnnouncements() {
    return ResponseEntity.ok(announcementService.getActiveAnnouncements());
  }

  @PostMapping("/create")
  public ResponseEntity<?> create(
      @Valid @RequestBody CreateAnnouncementRequestDto request, Principal principal) {
    UserEntity user = userRepository.findByUsername(principal.getName())
        .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

    if (!Boolean.TRUE.equals(user.getIsAdmin())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bu işlem için yetkiniz yok.");
    }

    return ResponseEntity
        .ok(announcementService.createAnnouncement(request.getTitle(), request.getContent(), request.getType()));
  }
}
