package com.lautaroroldan.epakerompimo.provider.info;

public record ITADGameResponse(
        String id,
        String slug,
        String title,
        String type,
        Boolean mature,
        ITADGameAssets assets
) {
}
