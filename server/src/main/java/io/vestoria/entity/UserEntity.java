package io.vestoria.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "users", uniqueConstraints = { @UniqueConstraint(columnNames = { "username", "email" }) })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity extends BaseEntity {
    private String username;

    private String password;

    private String email;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal balance;

    @Column(columnDefinition = "integer CHECK (level <= 100)")
    private Integer level;

    private Long xp;

    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean isAdmin = false;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BuildingEntity> buildings;
}
