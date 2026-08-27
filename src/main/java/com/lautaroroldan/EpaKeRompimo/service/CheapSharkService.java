package com.lautaroroldan.EpaKeRompimo.service;

import com.lautaroroldan.EpaKeRompimo.cheapshark.CheapSharkDealDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Service
public class CheapSharkService {
    private final RestClient client;

    public CheapSharkService(RestClient cheapSharkClient) {
        this.client = cheapSharkClient;
    }

    public List<CheapSharkDealDTO> searchDeals(String title, String storeIDs, Integer exact){
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/deals")
                        .queryParam("title",title)
                        .queryParam("exact",exact)
                        .queryParamIfPresent("storeID", Optional.ofNullable(storeIDs))
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<CheapSharkDealDTO>>() {
                });
    }

}
