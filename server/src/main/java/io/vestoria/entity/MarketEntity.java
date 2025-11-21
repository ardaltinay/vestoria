package io.vestoria.entity;

import jakarta.persistence.Column;
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

import java.math.BigDecimal;

@Entity
@Table(name = "market_items", indexes = {
        @Index(name = "idx_market_items_seller_id", columnList = "seller_id"),
        @Index(name = "idx_market_items_item_id", columnList = "item_id"),
        @Index(name = "idx_market_items_is_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketEntity extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "seller_id")
    private UserEntity seller;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private ItemEntity item;

    @Column(precision = 18, scale = 2)
    private BigDecimal price;

    private Integer quantity;

    @Column(name = "is_active")
    private Boolean isActive;

    @jakarta.persistence.Version
    private Long version;
}
