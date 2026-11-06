package com.lautaroroldan.epakerompimo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Una empresa desarrolladora favorita de un usuario. Las empresas vienen solo de RAWG.
// Un mismo usuario no puede tener la misma empresa dos veces.
@Entity
@Table(
        name = "favorite_enterprises",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_favorite_enterprise_user_rawg",
                columnNames = {"user_id", "rawg_enterprise_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class FavoriteEnterprise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Id de la empresa en RAWG. No es una clave foránea de MySQL.
    @Column(name = "rawg_enterprise_id", nullable = false)
    private Long rawgEnterpriseId;
}
