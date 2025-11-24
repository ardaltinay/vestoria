package io.vestoria.entity;

import io.vestoria.enums.ItemTier;
import io.vestoria.enums.ItemUnit;
import io.vestoria.enums.ItemCategory;
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

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "items", indexes = {
        @Index(name = "idx_items_name", columnList = "name"),
        @Index(name = "idx_items_building_id", columnList = "building_id")
})
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

    @Column(precision = 5, scale = 2)
    private BigDecimal qualityScore;

    private Long demand;// talep

    private Long supply;// arz

    @Enumerated(EnumType.STRING)
    private ItemTier tier;

    @Enumerated(EnumType.STRING)
    private ItemCategory category;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "building_id")
    private BuildingEntity building;
}
