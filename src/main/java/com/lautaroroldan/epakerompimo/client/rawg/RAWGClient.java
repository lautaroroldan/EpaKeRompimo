package com.lautaroroldan.epakerompimo.client.rawg;

import com.lautaroroldan.epakerompimo.dto.rawg.RAWGGameDetailsDTO;
import com.lautaroroldan.epakerompimo.dto.rawg.RAWGGamesDTO;
import com.lautaroroldan.epakerompimo.dto.rawg.RAWGPagination;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RAWGClient {

    private final RestClient client;

    public RAWGClient(@Qualifier("rawgClient") RestClient client) {
        this.client = client;
    }

    public RAWGPagination<RAWGGamesDTO> listOfGames(String title) {
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/games")
                        .queryParam("search", title)
                        .queryParam("key", "{key}")
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<RAWGPagination<RAWGGamesDTO>>() {
                });
    }

    // Get a list of DLC's for the game, GOTY and other editions, companion apps, etc.
    public RAWGPagination<RAWGGamesDTO> listOfDLCs(Integer gamePk) {
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/games/" + gamePk + "/additions")
                        .queryParam("key", "{key}")
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<RAWGPagination<RAWGGamesDTO>>() {
                });
    }

    public RAWGGameDetailsDTO gameDetails(Integer id) {
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/games/" + id)
                        .queryParam("key", "{key}")
                        .build())
                .retrieve()
                .body(RAWGGameDetailsDTO.class);
    }
}
