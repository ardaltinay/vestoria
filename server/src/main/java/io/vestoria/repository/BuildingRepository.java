package io.vestoria.repository;

import io.vestoria.entity.BuildingEntity;
import io.vestoria.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BuildingRepository extends JpaRepository<BuildingEntity, UUID> {
  List<BuildingEntity> findByOwnerId(UUID ownerId);

  long countByOwnerAndStatus(UserEntity owner, String status);

  long countByStatus(String status);
}
