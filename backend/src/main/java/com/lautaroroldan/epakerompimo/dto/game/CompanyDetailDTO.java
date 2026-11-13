package com.lautaroroldan.epakerompimo.dto.game;

public record CompanyDetailDTO(
        long id,
        String name,
        String slug,
        String description,
        Integer foundedYear,
        String logoUrl,
        String websiteUrl
) {
}
