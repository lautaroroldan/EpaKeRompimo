package com.lautaroroldan.epakerompimo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Corrección del link "Ir a la tienda" para un juego y una tienda puntuales.
 *
 * <p>ITAD devuelve la URL del deal y a veces apunta a una edición o a un juego
 * distinto. El administrador puede guardar la URL correcta o directamente
 * ocultar la oferta si no existe una URL válida.</p>
 */
@Entity
@Table(
        name = "store_link_overrides",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_store_link_title_store",
                columnNames = {"title_key", "store_name"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class StoreLinkOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mismo título normalizado que GameMatchOverride
    @Column(name = "title_key", nullable = false, length = 255)
    private String titleKey;

    @Column(name = "display_title", length = 255)
    private String displayTitle;

    // Nombre de la tienda tal como lo devuelve ITAD (por ejemplo "Steam")
    @Column(name = "store_name", nullable = false, length = 100)
    private String storeName;

    // URL corregida. Si es null y hidden es true, la oferta no se muestra.
    @Column(name = "url", length = 2048)
    private String url;

    @Column(nullable = false)
    private boolean hidden = false;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;
}
