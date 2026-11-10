package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.GameDTO;
import com.lautaroroldan.epakerompimo.dto.GameWithPricesDTO;
import com.lautaroroldan.epakerompimo.dto.PriceDTO;
import com.lautaroroldan.epakerompimo.dto.game.GameDetailDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GameSearchServiceManager implements GameSearchService{

    private static final Logger log = LoggerFactory.getLogger(GameSearchServiceManager.class);

    private final GameCatalogService gameCatalogService;
    private final PriceAggregationService priceAggregationService;
    private final GameDetailService gameDetailService;

    public GameSearchServiceManager(GameCatalogService gameCatalogService,
                                    PriceAggregationService priceAggregationService,
                                    GameDetailService gameDetailService) {
        this.gameCatalogService = gameCatalogService;
        this.priceAggregationService = priceAggregationService;
        this.gameDetailService = gameDetailService;
    }

    private List<GameDTO> findGameByTitle(String title){
        return gameCatalogService.searchByTitle(title);
    }

    @Override
    public List<GameWithPricesDTO> findDeals(String country, Integer offset, Integer limit) {
        return priceAggregationService.findDeals(country, offset, limit);
    }

    @Override
    public List<GameWithPricesDTO> findPriceByGameTitle(String title, String country) {
        List<GameDTO> games = gameCatalogService.searchByTitle(title);
        String[] gameIds = games.stream().map(GameDTO::id).toArray(String[]::new);
        Map<String, List<PriceDTO>> pricesByGameId = priceAggregationService.pricesByGameId(gameIds, country);

        return games.stream()
                .map(game -> new GameWithPricesDTO(game, pricesByGameId.getOrDefault(game.id(), List.of())))
                .toList();
    }

    @Override
    public GameDetailDTO findGameDetailByTitle(String title) {
        return gameDetailService.findByTitle(title);
    }

}
