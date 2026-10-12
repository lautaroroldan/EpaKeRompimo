package com.lautaroroldan.epakerompimo.dto;

import java.util.List;

public record GameWithPricesDTO(
        GameDTO game,
        List<PriceDTO> prices
) {
}
