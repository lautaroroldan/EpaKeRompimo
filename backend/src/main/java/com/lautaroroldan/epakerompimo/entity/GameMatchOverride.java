package com.lautaroroldan.epakerompimo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Juego "fijado" a mano por el administrador: cuando el matcheo automático elige
 * el juego equivocado, esta fila le dice al matcher qué id usar en cada proveedor.
 *
 * <p>La clave es {@code titleKey}, el título normalizado que usa el perfil
 * (por ejemplo {@code elden ring}). Así, una sola corrección arregla la
 * descripción y los precios de esa URL para todos los usuarios.</p>
 */
@Entity
@Table(
        name = "game_match_overrides",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_match_override_title",
                columnNames = {"title_key"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class GameMatchOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title_key", nullable = false, length = 255)
    private String titleKey;

    // Título original tal como lo escribió el usuario, para que el admin se ubique
    @Column(name = "display_title", length = 255)
    private String displayTitle;

    // Juego correcto en IGDB (arregla la descripción y los metadatos)
    @Column(name = "igdb_game_id")
    private Long igdbGameId;

    // Juego correcto en ITAD (arregla la comparación de precios)
    @Column(name = "itad_game_id", length = 64)
    private String itadGameId;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;
}
