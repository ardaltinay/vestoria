package io.vestoria.repository;

import io.vestoria.entity.BuildingEntity;
import io.vestoria.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BuildingRepository extends JpaRepository<BuildingEntity, UUID> {
  List<BuildingEntity> findByOwnerId(UUID ownerId);

  @Query("SELECT b FROM BuildingEntity b LEFT JOIN FETCH b.items WHERE b.type = 'SHOP'")
  List<BuildingEntity> findAllShopsWithItems();

  long countByOwnerAndStatus(UserEntity owner, String status);

  long countByStatus(String status);
}
