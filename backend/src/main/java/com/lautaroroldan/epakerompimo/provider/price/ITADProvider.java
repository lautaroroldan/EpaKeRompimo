package com.lautaroroldan.epakerompimo.provider.price;

import com.lautaroroldan.epakerompimo.client.AbstractApiClient;
import com.lautaroroldan.epakerompimo.dto.GameDTO;
import com.lautaroroldan.epakerompimo.dto.GameWithPricesDTO;
import com.lautaroroldan.epakerompimo.dto.PriceDTO;
import com.lautaroroldan.epakerompimo.dto.StoreDTO;
import com.lautaroroldan.epakerompimo.provider.info.GameInfoProvider;
import com.lautaroroldan.epakerompimo.provider.info.ITADGameResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ITADProvider extends AbstractApiClient implements PriceProvider, GameInfoProvider {

    private final RestClient client;

    public ITADProvider(@Qualifier("ITADClient") RestClient client) {
        this.client = client;
    }

    @Override
    public Map<String, List<PriceDTO>> findPriceById(String [] gameIds, String country) {
        log.info("responses:", gameIds, country);
        return ApiConnect(() -> {
            if (gameIds == null || gameIds.length == 0) return Map.of();

            List<ITADPriceResponse> responses = client.post()
                    .uri(uri -> uri.path("/games/prices/v3")
                            .queryParam("country", country)
                            .build())
                    .body(gameIds)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ITADPriceResponse>>() {});

            return responses.stream()
                    .collect(Collectors.toMap(
                            ITADPriceResponse::id,
                            response -> response.deals().stream()
                                    .map(this::toPriceDTO)
                                    .toList()
                    ));
        }, Map.of());
    }

    @Override
    public String getName() {
        return "ITAD";
    }

    @Override
    public List<GameWithPricesDTO> findDeals(String country, Integer offset, Integer limit) {
        return ApiConnect(() -> {
            ITADDealResponse response = client.get()
                    .uri(uri -> uri.path("/deals/v2")
                            .queryParam("country", country)
                            .queryParam("offset", offset)
                            .queryParam("limit", limit)
                            .build())
                    .retrieve()
                    .body(ITADDealResponse.class);

            if(response == null || response.list() == null) return List.of();

            return response.list().stream()
                    .map(game -> new GameWithPricesDTO(toGameDTO(game.game()), List.of(toPriceDTO(game.deal())))
                    )
                    .toList();
        }, List.of());
    }

    private PriceDTO toPriceDTO(ITADDeal deal) {
        return new PriceDTO(BigDecimal.valueOf(deal.price().amount()), BigDecimal.valueOf(deal.regular().amount()), new StoreDTO(deal.shop().name(), deal.url(), ""), getName());
    }

    @Override
    public List<PriceDTO> findPrice(String titulo) {
        return List.of();
    }

    @Override
    public List<GameDTO> searchGame(String title) {
        return ApiConnect(() -> {
            List<ITADGameResponse> responses = client.get()
                    .uri(uri -> uri.path("/games/search/v1")
                            .queryParam("title", title)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ITADGameResponse>>() {});

            log.info(responses.toString());
            return responses.stream()
                    .filter(x -> x.type()!= null && x.type().equals("game"))
                    .map(this::toGameDTO)
                    .toList();
        }, List.of());
    }

    @Override
    public GameDTO findGameById(String id) {
        return null;
    }

    private GameDTO toGameDTO(ITADGameResponse game) {
        return new GameDTO(game.id(), game.title(), game.slug(), game.type(), game.assets().banner300(), "", new String[]{""});
    }


}
