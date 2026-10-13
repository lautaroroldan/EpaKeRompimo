package com.lautaroroldan.epakerompimo.dto;

import java.math.BigDecimal;

public record PriceDTO(
        BigDecimal salePrice,
        BigDecimal normalPrice,
        StoreDTO store,
        String providerName
) {
}
