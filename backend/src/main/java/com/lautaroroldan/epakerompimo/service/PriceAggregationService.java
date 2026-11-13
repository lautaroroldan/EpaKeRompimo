package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.GameWithPricesDTO;
import com.lautaroroldan.epakerompimo.dto.PriceDTO;
import com.lautaroroldan.epakerompimo.provider.price.PriceProvider;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PriceAggregationService {
    private final List<PriceProvider> priceProviders;

    public PriceAggregationService(List<PriceProvider> priceProviders) {
        this.priceProviders = priceProviders;
    }

    public List<GameWithPricesDTO> findDeals(String country, Integer offset, Integer limit, String sort) {
        return priceProviders.stream()
                .flatMap(provider -> provider.findDeals(country, offset, limit, sort).stream())
                .toList();
    }

    public Map<String, List<PriceDTO>> pricesByGameId(String[] gameIds, String country) {
        Map<String, List<PriceDTO>> result = new HashMap<>();
        for (PriceProvider provider : priceProviders) {
            provider.findPriceById(gameIds, country).forEach((gameId, prices) ->
                    result.merge(gameId, prices, (existing, incoming) -> {
                        existing.addAll(incoming);
                        return existing;
                    }));
        }
        return result;
    }
}
