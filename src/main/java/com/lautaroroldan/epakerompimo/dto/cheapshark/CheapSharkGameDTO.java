package com.lautaroroldan.epakerompimo.dto.cheapshark;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CheapSharkGameDTO(
        String gameID,
        String steamAppID,
        String cheapest,
        String cheapestDealID,
        String external,
        String internalName,
        String thumb
) {
}
