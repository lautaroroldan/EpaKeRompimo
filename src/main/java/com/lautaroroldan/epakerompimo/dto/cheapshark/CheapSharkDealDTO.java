package com.lautaroroldan.epakerompimo.dto.cheapshark;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CheapSharkDealDTO(
        String title,
        String salePrice,
        String normalPrice,
        String savings,
        String storeID
) {
}
