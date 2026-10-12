package com.lautaroroldan.epakerompimo.dto;

public record GameDTO(
        String id,
        String title,
        String description,
        String type,
        String imageUrl,
        String releaseDate,
        String[] tags
) {
}
