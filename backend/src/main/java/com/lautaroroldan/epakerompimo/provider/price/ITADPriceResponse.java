package com.lautaroroldan.epakerompimo.provider.price;

import java.util.List;

public record ITADPriceResponse(
        String id,
        ITADHistoryLow historyLow,
        List<ITADDeal> deals
) {}
