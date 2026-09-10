package com.lautaroroldan.epakerompimo.client.cheapshark;

import com.lautaroroldan.epakerompimo.dto.cheapshark.CheapSharkGameDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class CheapSharkGameClient {

    private final RestClient client;

    public CheapSharkGameClient(@Qualifier("cheapSharkClient") RestClient client) {
        this.client = client;
    }

    public List<CheapSharkGameDTO> listOfGames(String title) {
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
