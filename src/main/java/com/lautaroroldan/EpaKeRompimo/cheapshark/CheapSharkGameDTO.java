package com.lautaroroldan.EpaKeRompimo.cheapshark;

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
