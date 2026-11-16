package com.lautaroroldan.epakerompimo.dto.admin;

import java.time.Instant;
import java.util.List;

/**
 * Estado de las correcciones manuales de un título. Si no hay ninguna, los ids
 * vienen en {@code null} y {@code storeLinks} vacío.
 */
public record MatchOverrideResponse(
        String displayTitle,
        Long igdbGameId,
        String itadGameId,
        Instant updatedAt,
        List<StoreLinkOverrideResponse> storeLinks
) {}
