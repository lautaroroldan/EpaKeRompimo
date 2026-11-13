package com.lautaroroldan.epakerompimo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Una empresa desarrolladora favorita de un usuario. Se identifica por su id de IGDB, porque IGDB es
// lo que permite buscar empresas por nombre (ITAD no).
// Un mismo usuario no puede tener la misma empresa dos veces.
@Entity
@Table(
        name = "favorite_enterprises",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_favorite_enterprise_user_igdb",
                columnNames = {"user_id", "igdb_company_id"}
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

    // Id de la empresa en IGDB. No es una clave foránea de MySQL.
    @Column(name = "igdb_company_id", nullable = false)
    private Long igdbCompanyId;

    @Column(name = "name", nullable = false)
    private String name;

    // URL del logo en IGDB. Se busca una sola vez al guardar la empresa; es null si IGDB no tiene logo.
    @Column(name = "logo_url", length = 500)
    private String logoUrl;
}
