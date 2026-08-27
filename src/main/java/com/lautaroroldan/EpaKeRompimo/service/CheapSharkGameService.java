package com.lautaroroldan.EpaKeRompimo.service;

import com.lautaroroldan.EpaKeRompimo.cheapshark.CheapSharkGameDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class CheapSharkGameService {
    private final RestClient client;

    public CheapSharkGameService(RestClient client) {
        this.client = client;
    }

    public List<CheapSharkGameDTO> listOfGames(String title){
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/games")
                        .queryParam("title", title)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<CheapSharkGameDTO>>() {
                });
    }
}
