package io.vestoria.repository;

import io.vestoria.entity.TransactionEntity;
import io.vestoria.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {
  @Query("SELECT SUM(t.price) FROM TransactionEntity t WHERE t.seller = :seller AND t.createdTime > :date")
  BigDecimal sumEarningsBySellerAndCreatedAtAfter(@Param("seller") UserEntity seller,
      @Param("date") LocalDateTime date);

  @Query("SELECT SUM(t.amount) FROM TransactionEntity t WHERE t.itemName = :itemName AND t.createdTime > :date")
  Integer sumAmountByItemNameAndCreatedAtAfter(@Param("itemName") String itemName,
      @Param("date") LocalDateTime date);
}
