package com.lautaroroldan.epakerompimo.dto.game;

import java.math.BigDecimal;

public record GameOfferDTO(String dealId, int storeId, String storeName, BigDecimal priceUsd,
                           BigDecimal retailPriceUsd, BigDecimal savingsPercent, String url) { }
