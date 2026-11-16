package com.lautaroroldan.epakerompimo.dto.report;

import com.lautaroroldan.epakerompimo.enums.MatchReportStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Resolución del administrador. Los ids y la URL son opcionales: si vienen, se
 * guardan como corrección permanente ("juego fijado") y el matcher los usa
 * desde ese momento.
 */
public record MatchReportResolveRequest(
        @NotNull MatchReportStatus status,
        @Size(max = 1000) String adminNote,
        Long igdbGameId,
        @Size(max = 64) String itadGameId,
        @Size(max = 100) String storeName,
        @Size(max = 2048) String storeUrl,
        Boolean hideOffer
) {}
