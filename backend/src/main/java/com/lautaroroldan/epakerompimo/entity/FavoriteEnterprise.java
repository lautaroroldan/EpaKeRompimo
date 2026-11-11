package com.lautaroroldan.epakerompimo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Una empresa desarrolladora favorita de un usuario (el id y el nombre vienen de ITAD).
// Un mismo usuario no puede tener la misma empresa dos veces.
@Entity
@Table(
        name = "favorite_enterprises",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_favorite_enterprise_user_itad",
                columnNames = {"user_id", "itad_enterprise_id"}
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

    // Id de la empresa en ITAD. No es una clave foránea de MySQL.
    @Column(name = "itad_enterprise_id", nullable = false)
    private Long itadEnterpriseId;

    // ITAD no permite consultar una empresa por id, por eso el nombre se guarda acá
    @Column(name = "name", nullable = false)
    private String name;
}
