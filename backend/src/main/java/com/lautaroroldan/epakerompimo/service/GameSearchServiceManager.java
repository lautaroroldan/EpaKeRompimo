package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.GameDTO;
import com.lautaroroldan.epakerompimo.dto.GameWithPricesDTO;
import com.lautaroroldan.epakerompimo.dto.PriceDTO;
import com.lautaroroldan.epakerompimo.dto.game.GameDetailDTO;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GameSearchServiceManager implements GameSearchService{

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

    /**
     * Every store's price for the game that best matches the title, cheapest
     * first, so the profile never shows a DLC or a different release that ITAD
     * might rank higher.
     */
    @Override
    public List<PriceDTO> findOffersByTitle(String title, String country) {
        GameDTO game = preferExactTitle(gameCatalogService.searchByTitle(title), title);
        if (game == null) {
            return List.of();
        }
        return priceAggregationService
                .pricesByGameId(new String[]{game.id()}, country)
                .getOrDefault(game.id(), List.of())
                .stream()
                .filter(price -> price.salePrice() != null)
                .sorted(Comparator.comparing(PriceDTO::salePrice))
                // A shop can quote the same game twice (different DRM or
                // platform); the sort already left the cheapest one first.
                .collect(Collectors.toMap(price -> price.store().name(), price -> price,
                        (cheapest, duplicate) -> cheapest, LinkedHashMap::new))
                .values()
                .stream()
                .toList();
    }

    private static GameDTO preferExactTitle(List<GameDTO> candidates, String title) {
        GameDTO exact = candidates.stream()
                .filter(candidate -> TitleNormalizer.sameTitle(candidate.title(), title))
                .findFirst()
                .orElse(null);
        return exact != null ? exact : candidates.stream().findFirst().orElse(null);
    }
}
