package io.vestoria.entity;

import io.vestoria.enums.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "market_transactions", indexes = {
        @Index(name = "idx_transactions_buyer_id", columnList = "buyer_id"),
        @Index(name = "idx_transactions_market_item_id", columnList = "market_item_id"),
        @Index(name = "idx_transactions_item_name", columnList = "itemName"),
        @Index(name = "idx_transactions_created_time", columnList = "createdTime")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEntity extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(precision = 18, scale = 2)
    private BigDecimal price;

    private Integer amount;

    private String itemName;

    @ManyToOne(optional = false)
    @JoinColumn(name = "buyer_id")
    private UserEntity buyer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "seller_id")
    private UserEntity seller;

    @ManyToOne
    @JoinColumn(name = "market_item_id")
    private MarketEntity marketItem;
}
