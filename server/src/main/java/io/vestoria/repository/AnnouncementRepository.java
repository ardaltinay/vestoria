package io.vestoria.repository;

import io.vestoria.entity.AnnouncementEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnouncementRepository extends JpaRepository<AnnouncementEntity, UUID> {
    List<AnnouncementEntity> findAllByIsActiveTrueOrderByCreatedTimeDesc();
}
