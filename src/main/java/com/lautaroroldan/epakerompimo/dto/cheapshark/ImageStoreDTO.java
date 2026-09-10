package com.lautaroroldan.epakerompimo.dto.cheapshark;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ImageStoreDTO(
        String banner,
        String logo,
        String icon
) {
}
