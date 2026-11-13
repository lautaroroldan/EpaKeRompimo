package com.lautaroroldan.epakerompimo.provider.price;

import com.lautaroroldan.epakerompimo.client.AbstractApiClient;
import com.lautaroroldan.epakerompimo.dto.GameDTO;
import com.lautaroroldan.epakerompimo.dto.GameWithPricesDTO;
import com.lautaroroldan.epakerompimo.dto.PriceDTO;
import com.lautaroroldan.epakerompimo.dto.StoreDTO;
import com.lautaroroldan.epakerompimo.provider.gamecatalog.GameCatalogProvider;
import com.lautaroroldan.epakerompimo.provider.gamecatalog.ITADGameInfoResponse;
import com.lautaroroldan.epakerompimo.provider.gamecatalog.ITADGameResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ITADProvider extends AbstractApiClient implements PriceProvider, GameCatalogProvider {

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
                            response -> response.deals() == null ? List.of() : response.deals().stream()
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
    public List<GameWithPricesDTO> findDeals(String country, Integer offset, Integer limit, String sort) {
        return ApiConnect(() -> {
            ITADDealResponse response = client.get()
                    .uri(uri -> {
                        uri.path("/deals/v2")
                                .queryParam("country", country)
                                .queryParam("offset", offset)
                                .queryParam("limit", limit);
                        if (sort != null && !sort.isBlank()) {
                            uri.queryParam("sort", sort);
                        }
                        return uri.build();
                    })
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
        BigDecimal salePrice = toAmount(deal.price());
        BigDecimal normalPrice = toAmount(deal.regular());
        return new PriceDTO(salePrice, normalPrice,
                new StoreDTO(deal.shop().name(), deal.url(), ""), getName(),
                deal.cut(), deal.price() != null ? deal.price().currency() : null);
    }

    private static BigDecimal toAmount(ITADPrice price) {
        return price != null && price.amount() != null ? BigDecimal.valueOf(price.amount()) : null;
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

            if (responses == null) return List.of();

            return responses.stream()
                    .filter(x -> x.type()!= null && x.type().equals("game"))
                    .map(this::toGameDTO)
                    .toList();
        }, List.of());
    }

    @Override
    public GameDTO findGameById(String id) {
        return ApiConnect(() -> {
            ITADGameInfoResponse response = client.get()
                    .uri(uri -> uri.path("/games/info/v2")
                            .queryParam("id", id)
                            .build())
                    .retrieve()
                    .body(ITADGameInfoResponse.class);

            if (response == null) return null;

            String banner = response.assets() != null ? response.assets().banner300() : null;
            return new GameDTO(response.id(), response.title(), response.slug(), response.type(), banner, "", new String[]{""});
        }, null);
    }

    private GameDTO toGameDTO(ITADGameResponse game) {
        return new GameDTO(game.id(), game.title(), game.slug(), game.type(), game.assets().banner300(), "", new String[]{""});
    }


}
