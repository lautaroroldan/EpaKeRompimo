package com.lautaroroldan.EpaKeRompimo.cheapshark;

public record CheapSharkDealDTO(
        String title,
        String salePrice,
        String normalPrice,
        String savings,
        String storeID
) {
}
