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
        List<IGDBNamed> platforms,
        List<IGDBNamed> genres,
        List<IGDBNamed> themes,
        List<IGDBNamed> keywords,
        List<IGDBScreenshot> screenshots,
        List<IGDBVideo> videos,
        List<IGDBInvolvedCompany> involved_companies
) {

    public record IGDBCover(String url) {
    }

    /** Reference entities exposed by IGDB that only contribute a display name. */
    public record IGDBNamed(String name) {
    }

    public record IGDBScreenshot(String url) {
    }

    public record IGDBVideo(String video_id, String name) {
    }

    /**
     * Credit row between a game and a company. {@code id} is the row's own id,
     * not the company id, and it preserves IGDB's credit order.
     */
    public record IGDBInvolvedCompany(long id, IGDBCompany company, boolean developer, boolean publisher) {
    }

    public record IGDBCompany(long id, String name) {
    }
}
