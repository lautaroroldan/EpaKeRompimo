package com.lautaroroldan.epakerompimo.client.cheapshark;

import com.lautaroroldan.epakerompimo.dto.cheapshark.CheapSharkDealDTO;
import com.lautaroroldan.epakerompimo.dto.cheapshark.CheapSharkDealFilterDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class CheapSharkDealClient {

    private final RestClient client;

    public CheapSharkDealClient(@Qualifier("cheapSharkClient") RestClient client) {
        this.client = client;
    }

    public List<CheapSharkDealDTO> searchDeals(CheapSharkDealFilterDTO filters) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        filters.toParamMap().forEach((k, v) -> params.add(k, v.toString()));
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/deals")
                        .queryParams(params)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<CheapSharkDealDTO>>() {
                });
    }
}
