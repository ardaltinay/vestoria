package io.vestoria.repository;

import io.vestoria.dto.response.CategoryBreakdownDto;
import io.vestoria.dto.response.DailyFinancialDto;
import io.vestoria.dto.response.TrendingItemDto;
import io.vestoria.entity.TransactionEntity;
import io.vestoria.entity.UserEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {
    @Query("SELECT SUM(t.price) FROM TransactionEntity t WHERE t.seller = :seller AND t.createdTime > :date")
    BigDecimal sumEarningsBySellerAndCreatedAtAfter(@Param("seller") UserEntity seller,
            @Param("date") LocalDateTime date);

    @Query("SELECT SUM(t.amount) FROM TransactionEntity t WHERE t.itemName = :itemName AND t.createdTime > :date")
    Integer sumByItemNameAndCreatedAtAfter(@Param("itemName") String itemName, @Param("date") LocalDateTime date);

    @Query("SELECT SUM(t.price) FROM TransactionEntity t WHERE t.buyer = :buyer AND t.createdTime > :date")
    BigDecimal sumExpensesByBuyerAndCreatedAtAfter(@Param("buyer") UserEntity buyer, @Param("date") LocalDateTime date);

    @Query("SELECT new io.vestoria.dto.response.TrendingItemDto(t.itemName, SUM(t.amount)) FROM TransactionEntity t WHERE t.createdTime > :date GROUP BY t.itemName ORDER BY SUM(t.amount) DESC")
    List<TrendingItemDto> findTopTrendingItems(@Param("date") LocalDateTime date, Pageable pageable);

    @Query("SELECT new io.vestoria.dto.response.DailyFinancialDto(CAST(t.createdTime AS LocalDate), SUM(t.price)) FROM TransactionEntity t WHERE t.seller = :user AND t.createdTime > :date GROUP BY CAST(t.createdTime AS LocalDate) ORDER BY CAST(t.createdTime AS LocalDate)")
    List<DailyFinancialDto> getDailyIncome(@Param("user") UserEntity user, @Param("date") LocalDateTime date);

    @Query("SELECT new io.vestoria.dto.response.DailyFinancialDto(CAST(t.createdTime AS LocalDate), SUM(t.price)) FROM TransactionEntity t WHERE t.buyer = :user AND t.createdTime > :date GROUP BY CAST(t.createdTime AS LocalDate) ORDER BY CAST(t.createdTime AS LocalDate)")
    List<DailyFinancialDto> getDailyExpense(@Param("user") UserEntity user, @Param("date") LocalDateTime date);

    @Query("SELECT new io.vestoria.dto.response.CategoryBreakdownDto(t.itemName, SUM(t.price)) FROM TransactionEntity t WHERE t.seller = :user AND t.createdTime > :date GROUP BY t.itemName ORDER BY SUM(t.price) DESC")
    List<CategoryBreakdownDto> getIncomeByCategory(@Param("user") UserEntity user, @Param("date") LocalDateTime date);
}
