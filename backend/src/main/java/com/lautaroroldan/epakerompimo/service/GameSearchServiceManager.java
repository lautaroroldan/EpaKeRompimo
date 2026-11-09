package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.GameDTO;
import com.lautaroroldan.epakerompimo.dto.GameWithPricesDTO;
import com.lautaroroldan.epakerompimo.dto.PriceDTO;
import com.lautaroroldan.epakerompimo.dto.game.GameDetailDTO;
import com.lautaroroldan.epakerompimo.provider.gamecatalog.GameCatalogProvider;
import com.lautaroroldan.epakerompimo.provider.info.GameInfoProvider;
import com.lautaroroldan.epakerompimo.provider.price.PriceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GameSearchServiceManager implements GameSearchService{

    private static final Logger log = LoggerFactory.getLogger(GameSearchServiceManager.class);
    private final List<PriceProvider> priceProviders;
    private final GameCatalogProvider gameCatalogProvider;
    private final GameInfoProvider gameInfoProvider;


    public GameSearchServiceManager(List<PriceProvider> priceProviders, GameCatalogProvider gameCatalogProvider, GameInfoProvider gameInfoProvider) {
        this.priceProviders = priceProviders;
        this.gameCatalogProvider = gameCatalogProvider;
        this.gameInfoProvider = gameInfoProvider;
    }

    private List<GameDTO> findGameByTitle(String title){
        List<GameDTO> games = gameCatalogProvider.searchGame(title);
        return games;
    }

    @Override
    public List<GameWithPricesDTO> findDeals(String country, Integer offset, Integer limit) {
        return priceProviders.stream()
                .flatMap(p -> p.findDeals(country, offset, limit).stream())
                .toList();
    }



    @Override
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

    @Override
    public GameDetailDTO findGameDetailByTitle(String title) {
        return gameInfoProvider.searchGame(title).stream().findFirst().orElse(null);
    }

}
