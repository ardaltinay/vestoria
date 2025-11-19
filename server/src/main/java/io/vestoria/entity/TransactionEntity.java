package io.vestoria.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "market_transactions", indexes = {
        @Index(name = "idx_transactions_buyer_id", columnList = "buyer_id"),
        @Index(name = "idx_transactions_market_item_id", columnList = "market_item_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEntity extends BaseEntity {

    private String type;

    @ManyToOne(optional = false)
    @JoinColumn(name = "buyer_id")
    private UserEntity buyer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "market_item_id")
    private MarketEntity marketItem;
}
