package com.lautaroroldan.epakerompimo.dto.rawg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RatingDTO(
        Integer id,
        String title,
        Integer count,
        Double percent
) {
}
