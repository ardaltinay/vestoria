package io.vestoria.repository;

import io.vestoria.dto.response.MarketStatsDto;
import io.vestoria.entity.MarketEntity;
import io.vestoria.entity.UserEntity;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketRepository extends JpaRepository<MarketEntity, UUID> {

    @Query("SELECT m FROM MarketEntity m JOIN FETCH m.item WHERE m.isActive = true AND (:search IS NULL OR LOWER(m.item.name) LIKE :search)")
    Page<MarketEntity> findAllActiveWithDetails(@Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(m) FROM MarketEntity m JOIN m.item JOIN m.item.building WHERE m.item.building.id = :buildingId AND m.isActive = true")
    long countActiveListingsByBuilding(@Param("buildingId") UUID buildingId);

    @Modifying
    @Transactional
    void deleteByIsActiveFalseAndUpdatedTimeBefore(LocalDateTime dateTime);

    @Query("SELECT MIN(m.price) FROM MarketEntity m JOIN m.item WHERE m.item.name = :itemName AND m.isActive = true")
    BigDecimal findMinPriceByItemName(@Param("itemName") String itemName);

    @Query("SELECT COUNT(m) FROM MarketEntity m JOIN m.item WHERE m.item.name = :itemName AND m.isActive = true")
    long countActiveListingsByItemName(@Param("itemName") String itemName);

    @Query("SELECT COALESCE(SUM(m.quantity), 0) FROM MarketEntity m JOIN m.item WHERE m.item.name = :itemName AND m.isActive = true")
    long sumActiveQuantityByItemName(@Param("itemName") String itemName);

    @Query("SELECT SUM(m.quantity) FROM MarketEntity m JOIN m.item i WHERE i.name = :itemName AND m.createdTime > :date")
    Integer sumByItemNameAndCreatedAtAfter(@Param("itemName") String itemName, @Param("date") LocalDateTime date);

    @Query("SELECT new io.vestoria.dto.response.MarketStatsDto(m.item.name, MIN(m.price), COUNT(m)) "
            + "FROM MarketEntity m " + "WHERE m.item.name IN :itemNames AND m.isActive = true "
            + "GROUP BY m.item.name")
    List<MarketStatsDto> findMarketStatsByItemNames(@Param("itemNames") List<String> itemNames);

    Optional<MarketEntity> findBySellerAndItemNameAndPriceAndIsActiveTrue(UserEntity seller, String itemName,
            BigDecimal price);
}
