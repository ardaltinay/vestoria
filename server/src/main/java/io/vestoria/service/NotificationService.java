package io.vestoria.service;

import io.vestoria.dto.response.NotificationDto;
import io.vestoria.entity.NotificationEntity;
import io.vestoria.entity.UserEntity;
import io.vestoria.repository.NotificationRepository;
import io.vestoria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import io.vestoria.exception.ResourceNotFoundException;
import io.vestoria.exception.UnauthorizedAccessException;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  @Transactional
  @SuppressWarnings("null")
  public void createNotification(UserEntity user, String message) {
    NotificationEntity notification = NotificationEntity.builder()
        .user(user)
        .message(message)
        .isRead(false)
        .build();
    notificationRepository.save(notification);
  }

  @Transactional(readOnly = true)
  public List<NotificationDto> getUserNotifications(String username) {
    UserEntity user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

    return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
        .map(this::mapToDto)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public long getUnreadCount(String username) {
    UserEntity user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));
    return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
  }

  @Transactional
  @SuppressWarnings("null")
  public void markAsRead(UUID notificationId, String username) {
    NotificationEntity notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new ResourceNotFoundException("Bildirim bulunamadı"));

    if (!notification.getUser().getUsername().equals(username)) {
      throw new UnauthorizedAccessException("Bu bildirim size ait değil");
    }

    notification.setRead(true);
    notificationRepository.save(notification);
  }

  @Transactional
  public void markAllAsRead(String username) {
    UserEntity user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

    List<NotificationEntity> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    notifications.forEach(n -> n.setRead(true));
    notificationRepository.saveAll(notifications);
  }

  private NotificationDto mapToDto(NotificationEntity entity) {
    return NotificationDto.builder()
        .id(entity.getId())
        .message(entity.getMessage())
        .isRead(entity.isRead())
        .createdAt(entity.getCreatedTime())
        .build();
  }
}
