package com.lautaroroldan.epakerompimo.provider.info;

/**
 * Provider-shaped view of an IGDB company.
 * <p>
 * Field names mirror the IGDB wire format on purpose (same convention as
 * {@link IGDBGameResponse}). The {@code fields} clause of the Apicalypse query
 * must request exactly these fields: {@code name,logo.image_id}.
 */
public record IGDBCompanyResponse(
        long id,
        String name,
        IGDBLogo logo
) {

    public record IGDBLogo(String image_id) {
    }
}
