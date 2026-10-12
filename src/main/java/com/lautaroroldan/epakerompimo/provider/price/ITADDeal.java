package com.lautaroroldan.epakerompimo.provider.price;

import java.time.OffsetDateTime;
import java.util.List;

public record ITADDeal(
        ITADShop shop,
        ITADPrice price,
        ITADPrice regular,
        Integer cut,
        String voucher,
        ITADPrice storeLow,
        String flag,
        List<ITADDrm> drm,
        List<ITADPlatform> platforms,
        OffsetDateTime timestamp,
        OffsetDateTime expiry,
        String url
) {
}

