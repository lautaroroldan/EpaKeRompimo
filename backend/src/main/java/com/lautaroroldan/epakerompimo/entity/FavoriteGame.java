package com.lautaroroldan.epakerompimo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Un juego favorito de un usuario. No se guarda el juego completo: solo su id de ITAD.
// Un mismo usuario no puede tener el mismo juego dos veces.
@Entity
@Table(
        name = "favorite_games",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_favorite_game_user_itad",
                columnNames = {"user_id", "itad_game_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class FavoriteGame {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Id del juego en IsThereAnyDeal (UUID). No es una clave foránea de MySQL.
    @Column(name = "itad_game_id", nullable = false, length = 64)
    private String itadGameId;
}
