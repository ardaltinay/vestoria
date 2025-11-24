package io.vestoria.entity;

import io.vestoria.enums.BuildingStatus;
import io.vestoria.enums.BuildingSubType;
import io.vestoria.enums.BuildingType;
import io.vestoria.enums.BuildingTier;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "buildings", indexes = {
        @jakarta.persistence.Index(name = "idx_buildings_type", columnList = "type"),
        @jakarta.persistence.Index(name = "idx_buildings_owner_id", columnList = "owner_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildingEntity extends BaseEntity {
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private BuildingType type;

    @Enumerated(EnumType.STRING)
    private BuildingTier tier;

    @Enumerated(EnumType.STRING)
    private BuildingSubType subType;

    private Integer level;

    private BigDecimal productionRate;

    private Integer maxSlots;

    @Enumerated(EnumType.STRING)
    private BuildingStatus status;

    private BigDecimal cost;

    private Integer maxStock;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner;

    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemEntity> items;

    private LocalDateTime productionEndsAt;
    private LocalDateTime salesEndsAt;
    private Boolean isProducing;
    private Boolean isSelling;
    private BigDecimal lastRevenue;
}
