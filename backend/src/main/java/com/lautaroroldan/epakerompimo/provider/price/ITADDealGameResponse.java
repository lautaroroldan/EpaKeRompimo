package com.lautaroroldan.epakerompimo.provider.price;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.lautaroroldan.epakerompimo.provider.gamecatalog.ITADGameResponse;

public record ITADDealGameResponse(
        @JsonUnwrapped ITADGameResponse game,
        ITADDeal deal
) {
}
