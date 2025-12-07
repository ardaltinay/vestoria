package io.vestoria.service;

import io.vestoria.converter.AuthConverter;
import io.vestoria.converter.UserConverter;
import io.vestoria.dto.response.AuthResponseDto;
import io.vestoria.dto.response.DashboardStatsDto;
import io.vestoria.entity.UserEntity;
import io.vestoria.enums.BuildingStatus;
import io.vestoria.repository.BuildingRepository;
import io.vestoria.repository.TransactionRepository;
import io.vestoria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final TransactionRepository transactionRepository;
    private final BuildingRepository buildingRepository;
    private final UserConverter userConverter;
    private final AuthConverter authConverter;

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

        BigDecimal dailyExpenses = transactionRepository.sumExpensesByBuyerAndCreatedAtAfter(user, oneDayAgo);
        if (dailyExpenses == null)
            dailyExpenses = BigDecimal.ZERO;

        long activeBusinesses = buildingRepository.countByOwnerAndStatus(user, BuildingStatus.ACTIVE);
        long totalActiveBusinesses = buildingRepository.countByStatus(BuildingStatus.ACTIVE);

        double marketShare = 0.0;
        if (totalActiveBusinesses > 0) {
            marketShare = ((double) activeBusinesses / totalActiveBusinesses) * 100;
        }

        return userConverter.toDashboardStatsDto(dailyEarnings, dailyExpenses, (int) activeBusinesses, marketShare);
    }

    public AuthResponseDto getCurrentUser(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return authConverter.toResponseDto(user);
    }
}
