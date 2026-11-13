package com.lautaroroldan.epakerompimo.provider.info;

import java.util.List;

/**
 * Provider-shaped view of the IGDB company entity. Only the fields requested
 * by {@code IGDBProvider} are mapped.
 */
public record IGDBCompanyResponse(
        long id,
        String name,
        String slug,
        String description,
        Long start_date,
        IGDBLogo logo,
        List<IGDBWebsite> websites
) {

    public record IGDBLogo(String url) {
    }

    public record IGDBWebsite(String url) {
    }
}
