package io.vestoria.repository;

import io.vestoria.entity.MarketEntity;
import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

@Repository
public interface MarketRepository extends JpaRepository<MarketEntity, UUID> {
  List<MarketEntity> findBySellerId(UUID sellerId);

  @Query("SELECT m FROM MarketEntity m JOIN FETCH m.seller JOIN FETCH m.item WHERE m.isActive = true AND (:search IS NULL OR LOWER(m.item.name) LIKE :search)")
  Page<MarketEntity> findAllActiveWithDetails(@Param("search") String search, Pageable pageable);

  @Query("SELECT COUNT(m) FROM MarketEntity m JOIN m.item i WHERE i.building.id = :buildingId AND m.isActive = true")
  long countActiveListingsByBuilding(@Param("buildingId") UUID buildingId);

  @Modifying
  @Transactional
  void deleteByIsActiveFalseAndUpdatedTimeBefore(LocalDateTime dateTime);
}
