package io.vestoria.repository;

import io.vestoria.entity.BuildingEntity;
import io.vestoria.entity.UserEntity;
import io.vestoria.enums.BuildingStatus;
import io.vestoria.enums.BuildingType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BuildingRepository extends JpaRepository<BuildingEntity, UUID> {
    List<BuildingEntity> findByOwnerId(UUID ownerId);

    @Query("SELECT b FROM BuildingEntity b LEFT JOIN FETCH b.items WHERE b.type = 'SHOP'")
    List<BuildingEntity> findAllShopsWithItems();

    long countByOwnerAndStatus(UserEntity owner, BuildingStatus status);

    long countByStatus(BuildingStatus status);

    List<BuildingEntity> findAllByIsSellingTrueAndSalesEndsAtBefore(LocalDateTime now);

    boolean existsByNameAndTypeAndOwnerId(String name, BuildingType type, UUID ownerId);
}
