package com.lautaroroldan.epakerompimo.dto.rawg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RAWGGamesDTO(
        Integer id,
        String slug,
        String name,
        String released,
        boolean tba,
        String background_image,
        Double rating,
        Integer rating_top,
        Integer ratings_count,
        String reviews_text_count,
        Integer added,
        Integer metacritic,
        String playtime,
        Integer suggestions_count,
        String updated
) {
}
