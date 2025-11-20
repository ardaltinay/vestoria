package io.vestoria.service;

import io.vestoria.dto.response.DashboardStatsDto;
import io.vestoria.entity.UserEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.vestoria.repository.BuildingRepository;
import io.vestoria.repository.TransactionRepository;
import io.vestoria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final NotificationService notificationService;
  private final TransactionRepository transactionRepository;
  private final BuildingRepository buildingRepository;

  @Transactional
  public void addXp(UserEntity user, long amount) {
    if (user == null)
      return;

    long currentXp = user.getXp() == null ? 0 : user.getXp();
    user.setXp(currentXp + amount);

    checkLevelUp(user);

    userRepository.save(user);
  }

  private void checkLevelUp(UserEntity user) {
    int currentLevel = user.getLevel() == null ? 1 : user.getLevel();
    long requiredXpForNext = currentLevel * 1000L;

    if (user.getXp() >= requiredXpForNext) {
      user.setLevel(currentLevel + 1);
      // Send notification
      String message = String.format("Tebrikler! Seviye atladınız. Yeni seviyeniz: %d", user.getLevel());
      notificationService.createNotification(user, message);
    }
  }

  public DashboardStatsDto getDashboardStats(UserEntity user) {
    LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
    BigDecimal dailyEarnings = transactionRepository.sumEarningsBySellerAndCreatedAtAfter(user, oneDayAgo);
    if (dailyEarnings == null)
      dailyEarnings = BigDecimal.ZERO;

    long activeBusinesses = buildingRepository.countByOwnerAndStatus(user, "ACTIVE");
    long totalActiveBusinesses = buildingRepository.countByStatus("ACTIVE");

    double marketShare = 0.0;
    if (totalActiveBusinesses > 0) {
      marketShare = ((double) activeBusinesses / totalActiveBusinesses) * 100;
    }

    return DashboardStatsDto.builder()
        .dailyEarnings(dailyEarnings)
        .activeBusinesses((int) activeBusinesses)
        .marketShare(marketShare)
        .build();
  }
}
