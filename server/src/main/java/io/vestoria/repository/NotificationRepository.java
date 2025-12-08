package io.vestoria.repository;

import io.vestoria.entity.NotificationEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {
    List<NotificationEntity> findByUserIdOrderByCreatedTimeDesc(UUID userId);

    long countByUserIdAndIsReadFalse(UUID userId);

    void deleteByCreatedTimeBefore(LocalDateTime date);
}
