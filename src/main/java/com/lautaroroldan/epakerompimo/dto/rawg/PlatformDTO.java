package com.lautaroroldan.epakerompimo.dto.rawg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlatformDTO(
        Integer id,
        String slug,
        String name
) {
}
