package io.vestoria.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vestoria.enums.ItemTier;
import io.vestoria.enums.ItemUnit;
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
@Table(name = "items", indexes = {@Index(name = "idx_items_name", columnList = "name"),
        @Index(name = "idx_items_building_id", columnList = "building_id"),
        @Index(name = "idx_items_owner_id", columnList = "owner_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemEntity extends BaseEntity {
    private String name;

    @Enumerated(EnumType.STRING)
    private ItemUnit unit;

    private BigDecimal price;

    private Integer quantity;

    @Column(precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(precision = 5, scale = 2)
    private BigDecimal qualityScore;

    @Enumerated(EnumType.STRING)
    private ItemTier tier;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "building_id")
    private BuildingEntity building;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner;

    private Boolean isProducing;
}
