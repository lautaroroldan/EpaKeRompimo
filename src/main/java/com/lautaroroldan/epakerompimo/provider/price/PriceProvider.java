package com.lautaroroldan.epakerompimo.provider.price;

import com.lautaroroldan.epakerompimo.dto.GameWithPricesDTO;
import com.lautaroroldan.epakerompimo.dto.PriceDTO;

import java.util.List;
import java.util.Map;

public interface PriceProvider {

    List<PriceDTO> findPrice(String titulo);
    Map<String, List<PriceDTO>> findPriceById(String [] gameIds, String country);
    String getName();
    List<GameWithPricesDTO> findDeals(String country, Integer offset, Integer limit);
}
