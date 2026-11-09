package com.lautaroroldan.epakerompimo.provider.gamecatalog;

public record ITADGameResponse(
        String id,
        String slug,
        String title,
        String type,
        Boolean mature,
        ITADGameAssets assets
) {
}
