package io.vestoria.repository;

import io.vestoria.dto.ItemAggregateDto;
import io.vestoria.entity.ItemEntity;
import io.vestoria.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, UUID> {

    @Query("SELECT new io.vestoria.dto.ItemAggregateDto(i.name, COUNT(i), COALESCE(SUM(i.quantity),0)) FROM ItemEntity i GROUP BY i.name")
    List<ItemAggregateDto> aggregateByName();

    Optional<ItemEntity> findByBuildingIdAndName(UUID buildingId, String name);

    List<ItemEntity> findAllByBuilding_Owner_Username(String username);

    List<ItemEntity> findAllByOwner_Username(String username);

    List<ItemEntity> findAllByOwner_UsernameAndBuildingIsNull(String username);

    Optional<ItemEntity> findByOwnerAndBuildingIsNullAndName(UserEntity owner, String name);

    long countByBuilding_Id(UUID buildingId);

    @Query("SELECT SUM(i.quantity) FROM ItemEntity i WHERE i.name = :itemName")
    Long sumQuantityByName(@Param("itemName") String itemName);

    long countByBuildingIdAndQuantityGreaterThan(UUID buildingId, int quantity);

    List<ItemEntity> findByOwnerIdAndBuildingIsNull(UUID ownerId);
}
