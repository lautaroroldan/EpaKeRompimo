package com.lautaroroldan.epakerompimo.provider.price;

import java.util.List;

public record ITADDealResponse(
        Integer nextOffset,
        boolean hasMore,
        List<ITADDealGameResponse> list

) {
}
