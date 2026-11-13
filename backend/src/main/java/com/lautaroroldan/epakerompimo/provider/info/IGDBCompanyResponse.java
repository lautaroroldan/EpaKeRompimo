package com.lautaroroldan.epakerompimo.provider.info;

import java.util.List;

/**
 * Provider-shaped view of the IGDB company entity.
 * <p>
 * Field names mirror the IGDB wire format on purpose (same convention as
 * {@link IGDBGameResponse}). The two consumers request different subsets:
 * the company profile asks for {@code logo.url} while the enterprise search
 * asks for {@code logo.image_id}, so the logo record carries both.
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

    public record IGDBLogo(String url, String image_id) {
    }

    public record IGDBWebsite(String url) {
    }
}
