package io.vestoria.entity;

import io.vestoria.enums.BuildingType;
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
import java.util.List;

@Entity
@Table(name = "buildings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildingEntity extends BaseEntity {

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private BuildingType type;

    private String subType;

    @Column(columnDefinition = "integer CHECK (level <= 5)")
    private Integer level;

    private String status;

    private BigDecimal cost;

    private Integer maxStock;

    private Long productionRate;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner;

    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemEntity> items;
}
