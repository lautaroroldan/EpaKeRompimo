package com.lautaroroldan.epakerompimo.entity;

import com.lautaroroldan.epakerompimo.enums.MatchReportStatus;
import com.lautaroroldan.epakerompimo.enums.MatchReportType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Consulta de un usuario sobre un matcheo equivocado: la descripción no es la
 * del juego o el link a la tienda apunta a otro lado.
 *
 * <p>Guarda un snapshot de lo que se estaba mostrando ({@code gameTitle},
 * ids y URL), así el administrador puede revisarlo aunque el matcheo
 * automático cambie después.</p>
 */
@Entity
@Table(name = "match_reports")
@Getter
@Setter
@NoArgsConstructor
public class MatchReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MatchReportType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchReportStatus status = MatchReportStatus.PENDING;

    // Título con el que se abrió el perfil (la clave que usa el matcher)
    @Column(name = "game_title", nullable = false, length = 255)
    private String gameTitle;

    @Column(name = "igdb_game_id")
    private Long igdbGameId;

    @Column(name = "itad_game_id", length = 64)
    private String itadGameId;

    // Solo para STORE_LINK_MISMATCH
    @Column(name = "store_name", length = 100)
    private String storeName;

    @Column(name = "current_url", length = 2048)
    private String currentUrl;

    @Column(length = 1000)
    private String message;

    @Column(name = "admin_note", length = 1000)
    private String adminNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;
}
