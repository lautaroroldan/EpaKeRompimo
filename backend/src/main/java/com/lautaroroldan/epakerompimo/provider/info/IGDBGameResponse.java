package com.lautaroroldan.epakerompimo.provider.info;

import java.util.List;

/**
 * Provider-shaped view of an IGDB game.
 * <p>
 * Field names mirror the IGDB wire format on purpose, so no mapping
 * annotations are needed. The {@code fields} clause of the Apicalypse query
 * must request exactly these fields.
 */
public record IGDBGameResponse(
        long id,
        String name,
        String slug,
        String summary,
        String storyline,
        Long first_release_date,
        IGDBCover cover,
        List<IGDBPlatform> platforms
) {

    public record IGDBCover(String url) {
    }

    public record IGDBPlatform(String name) {
    }
}
