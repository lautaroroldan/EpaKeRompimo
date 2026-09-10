package com.lautaroroldan.epakerompimo.client.cheapshark;

import com.lautaroroldan.epakerompimo.dto.cheapshark.CheapSharkStoreDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class CheapSharkStoreClient {

    private final RestClient client;

    public CheapSharkStoreClient(@Qualifier("cheapSharkClient") RestClient client) {
        this.client = client;
    }

    public List<CheapSharkStoreDTO> getAllStores() {
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stores")
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<CheapSharkStoreDTO>>() {
                });
    }
}
