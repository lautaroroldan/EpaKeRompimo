package com.lautaroroldan.epakerompimo.dto.cheapshark;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CheapSharkStoreDTO(
        String storeID,
        String storeName,
        String isActive,
        ImageStoreDTO images
) {
}
