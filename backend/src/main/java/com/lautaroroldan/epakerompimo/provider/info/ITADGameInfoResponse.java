package com.lautaroroldan.epakerompimo.provider.info;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Respuesta de GET /games/info/v2 de ITAD. Trae muchos más campos de los que usamos: se ignoran.
@JsonIgnoreProperties(ignoreUnknown = true)
public record ITADGameInfoResponse(
        String id,
        String slug,
        String title,
        String type,
        ITADGameAssets assets
) {
}
