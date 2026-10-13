package com.lautaroroldan.epakerompimo.provider.price;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.lautaroroldan.epakerompimo.provider.info.ITADGameResponse;

public record ITADDealGameResponse(
        @JsonUnwrapped ITADGameResponse game,
        ITADDeal deal
) {
}
