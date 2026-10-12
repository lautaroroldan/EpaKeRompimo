package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.GameDTO;
import com.lautaroroldan.epakerompimo.dto.GameWithPricesDTO;
import com.lautaroroldan.epakerompimo.dto.PriceDTO;
import com.lautaroroldan.epakerompimo.provider.info.GameInfoProvider;
import com.lautaroroldan.epakerompimo.provider.price.PriceProvider;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GameSearchService {

    private static final Logger log = LoggerFactory.getLogger(GameSearchService.class);
    private final List<PriceProvider> priceProviders;
    private final List<GameInfoProvider> gameInfoProviders;


    public GameSearchService(List<PriceProvider> priceProviders, List<GameInfoProvider> gameInfoProviders) {
        this.priceProviders = priceProviders;
        this.gameInfoProviders = gameInfoProviders;
    }

    public List<GameDTO> findGameByTitle(String title){
        List<GameDTO> games = gameInfoProviders.stream()
                .flatMap(g -> g.searchGame(title).stream())
                .toList();
        return games;
    }

    public List<GameWithPricesDTO> findDeals(String country, Integer offset, Integer limit) {
        return priceProviders.stream()
                .flatMap(p -> p.findDeals(country, offset, limit).stream())
                .toList();
    }



    public List<GameWithPricesDTO> findPriceByGameTitle(String title, String country) {
        List<GameDTO> games = findGameByTitle(title);
        String [] gameIds = games.stream().map(GameDTO::id).toArray(String[]::new);
        Map<String, List<PriceDTO>> pricesByGameId = new HashMap<>();
        for (PriceProvider priceProvider : priceProviders) {
            priceProvider.findPriceById(gameIds, country).forEach((gameId, prices) -> {
                pricesByGameId.merge(gameId, prices, (existingPrices, newPrices) -> {
                    existingPrices.addAll(newPrices);
                    return existingPrices;
                });
            });
        }

//        GAMEMATCHER
        return games.stream()
                .map(game -> new GameWithPricesDTO(game, pricesByGameId.getOrDefault(game.id(), List.of())))
                .toList();
    }
}
