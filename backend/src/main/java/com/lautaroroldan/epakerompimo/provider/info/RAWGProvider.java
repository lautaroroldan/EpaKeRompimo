package com.lautaroroldan.epakerompimo.provider.info;

import com.lautaroroldan.epakerompimo.client.AbstractApiClient;
import com.lautaroroldan.epakerompimo.dto.game.GameDetailDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class RAWGProvider extends AbstractApiClient implements GameInfoProvider {

    private final RestClient client;

    public RAWGProvider(@Qualifier("RAWGClient") RestClient client) {
        this.client = client;
    }


    @Override
    public List<GameDetailDTO> searchGame(String title) {
        return ApiConnect(() -> {
            RAWGPagination<RAWGGameDetailResponse> response;
            response = client.get()
                    .uri(uri -> uri.path("/games")
                            .queryParam("search", title)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<RAWGPagination<RAWGGameDetailResponse>>() {});
            List<RAWGGameDetailResponse> games = response.results();
            return games.stream()
                    .map(this::toGameDetailDTO)
                    .toList();
        }, new ArrayList<>());
    }

    @Override
    public GameDetailDTO findGameById(String id) {
        return null;
    }

    public GameDetailDTO toGameDetailDTO(RAWGGameDetailResponse response) {
        return new GameDetailDTO(
                response.id(),
                response.slug(),
                response.name(),
                response.description(),
                response.released(),
                response.background_image(),
                new ArrayList<>()
        );
    }
}
