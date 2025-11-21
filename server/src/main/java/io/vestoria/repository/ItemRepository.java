package io.vestoria.repository;

import io.vestoria.dto.ItemAggregateDto;
import io.vestoria.entity.ItemEntity;
import io.vestoria.enums.BuildingType;
import io.vestoria.enums.ItemTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, UUID> {

    @Query("SELECT new io.vestoria.dto.ItemAggregateDto(i.name, COUNT(i), COALESCE(SUM(i.quantity),0)) FROM ItemEntity i GROUP BY i.name")
    List<ItemAggregateDto> aggregateByName();

    Optional<ItemEntity> findByBuildingIdAndName(UUID buildingId, String name);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE ItemEntity i SET i.supply = :supply, i.demand = :demand, i.tier = :tier WHERE i.name = :name")
    int updateBulkItemsByName(String name, Long supply, Long demand, ItemTier tier);

    List<ItemEntity> findAllByBuilding_Owner_Username(String username);

    @Query("SELECT SUM(i.quantity) FROM ItemEntity i JOIN i.building b WHERE b.type = :type AND i.name = :name")
    Long sumSupplyByItemNameAndBuildingType(@Param("name") String name,
            @Param("type") BuildingType type);
}
