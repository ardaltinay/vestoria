package io.vestoria.repository;

import io.vestoria.entity.MarketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MarketRepository extends JpaRepository<MarketEntity, UUID> {
  List<MarketEntity> findBySellerId(UUID sellerId);

  @org.springframework.data.jpa.repository.Query("SELECT m FROM MarketEntity m JOIN FETCH m.seller JOIN FETCH m.item WHERE m.isActive = true")
  List<MarketEntity> findAllActiveWithDetails();
}
