package com.lautaroroldan.EpaKeRompimo.service;

import com.lautaroroldan.EpaKeRompimo.cheapshark.CheapSharkDealDTO;
import com.lautaroroldan.EpaKeRompimo.cheapshark.CheapSharkDealFilterDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Service
public class CheapSharkDealService {
    private final RestClient client;

    public CheapSharkDealService(RestClient cheapSharkClient) {
        this.client = cheapSharkClient;
    }

    public List<CheapSharkDealDTO> searchDeals(CheapSharkDealFilterDTO filters){
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
