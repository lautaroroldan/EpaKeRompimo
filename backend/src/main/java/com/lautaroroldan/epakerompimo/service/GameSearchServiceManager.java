package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.GameDTO;
import com.lautaroroldan.epakerompimo.dto.GameWithPricesDTO;
import com.lautaroroldan.epakerompimo.dto.PriceDTO;import com.lautaroroldan.epakerompimo.dto.game.GameDetailDTO;
import com.lautaroroldan.epakerompimo.entity.GameMatchOverride;
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
    private final GameMatchOverrideService matchOverrideService;

    public GameSearchServiceManager(GameCatalogService gameCatalogService,
                                    PriceAggregationService priceAggregationService,
                                    GameDetailService gameDetailService,
                                    GameMatchOverrideService matchOverrideService) {
        this.gameCatalogService = gameCatalogService;
        this.priceAggregationService = priceAggregationService;
        this.gameDetailService = gameDetailService;
        this.matchOverrideService = matchOverrideService;
    }

    @Override
    public List<GameWithPricesDTO> findDeals(String country, Integer offset, Integer limit, String sort) {
        return priceAggregationService.findDeals(country, offset, limit, sort);
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
        String gameId = resolveGameId(title);
        if (gameId == null) {
            return List.of();
        }
        List<PriceDTO> offers = priceAggregationService
                .pricesByGameId(new String[]{gameId}, country)
                .getOrDefault(gameId, List.of())
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

        // Links corregidos u ofertas ocultas por el administrador.
        return matchOverrideService.applyStoreOverrides(title, offers);
    }

    /**
     * Mismo criterio que {@link #findOffersByTitle}: así la estrella de favoritos del perfil
     * guarda el mismo juego cuyos precios se están mostrando. No consulta precios.
     */
    @Override
    public String findItadGameIdByTitle(String title) {
        return resolveGameId(title);
    }

    // ---------- Solo para el panel de administración ----------

    @Override
    public List<GameDetailDTO> findGameCandidates(String title) {
        return gameDetailService.searchCandidates(title);
    }

    @Override
    public List<GameDTO> findCatalogCandidates(String title) {
        return gameCatalogService.searchByTitle(title);
    }

    @Override
    public List<PriceDTO> findOffersByItadId(String itadGameId, String country) {
        if (itadGameId == null || itadGameId.isBlank()) {
            return List.of();
        }
        return priceAggregationService
                .pricesByGameId(new String[]{itadGameId}, country)
                .getOrDefault(itadGameId, List.of())
                .stream()
                .filter(price -> price.salePrice() != null)
                .sorted(Comparator.comparing(PriceDTO::salePrice))
                .toList();
    }

    /**
     * El id que fijó el administrador tiene prioridad; si no hay override, se
     * resuelve con el matcheo automático de siempre.
     */
    private String resolveGameId(String title) {
        GameMatchOverride override = matchOverrideService.findGameOverride(title).orElse(null);
        if (override != null && override.getItadGameId() != null && !override.getItadGameId().isBlank()) {
            return override.getItadGameId();
        }
        GameDTO game = preferExactTitle(gameCatalogService.searchByTitle(title), title);
        return game != null ? game.id() : null;
    }

    private static GameDTO preferExactTitle(List<GameDTO> candidates, String title) {
        GameDTO exact = candidates.stream()
                .filter(candidate -> TitleNormalizer.sameTitle(candidate.title(), title))
                .findFirst()
                .orElse(null);
        return exact != null ? exact : candidates.stream().findFirst().orElse(null);
    }
}
