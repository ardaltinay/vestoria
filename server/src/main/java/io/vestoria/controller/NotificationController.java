package io.vestoria.controller;

import io.vestoria.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<?> getNotifications(Principal principal) {
    return ResponseEntity.ok(notificationService.getUserNotifications(principal.getName()));
  }

  @GetMapping("/unread-count")
  public ResponseEntity<?> getUnreadCount(Principal principal) {
    return ResponseEntity.ok(notificationService.getUnreadCount(principal.getName()));
  }

  @PostMapping("/{id}/read")
  public ResponseEntity<?> markAsRead(@PathVariable UUID id, Principal principal) {
    notificationService.markAsRead(id, principal.getName());
    return ResponseEntity.ok().build();
  }

  @PostMapping("/read-all")
  public ResponseEntity<?> markAllAsRead(Principal principal) {
    notificationService.markAllAsRead(principal.getName());
    return ResponseEntity.ok().build();
  }
}
