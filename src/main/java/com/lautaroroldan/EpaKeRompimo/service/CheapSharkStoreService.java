package com.lautaroroldan.EpaKeRompimo.service;

import com.lautaroroldan.EpaKeRompimo.cheapshark.CheapSharkStoreDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class CheapSharkStoreService {

    private final RestClient client;

    public CheapSharkStoreService(RestClient cheapSharkClient) {
        this.client = cheapSharkClient;
    }

    public List<CheapSharkStoreDTO> searchStores(){
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stores")
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<CheapSharkStoreDTO>>() {
                });
    }

}
