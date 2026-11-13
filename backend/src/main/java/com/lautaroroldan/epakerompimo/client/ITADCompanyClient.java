package com.lautaroroldan.epakerompimo.client;

import com.lautaroroldan.epakerompimo.provider.gamecatalog.ITADCompany;
import com.lautaroroldan.epakerompimo.provider.gamecatalog.ITADGameCompaniesResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

// Pide a ITAD las empresas de un juego. Usa el mismo cliente que ITADProvider ("ITADClient"),
// que ya manda la API key en el header ITAD-API-Key.
// Si ITAD falla, ApiConnect lo registra en el log y devuelve una lista vacía (no rompe la app).
@Component
public class ITADCompanyClient extends AbstractApiClient {

    private final RestClient client;

    public ITADCompanyClient(@Qualifier("ITADClient") RestClient client) {
        this.client = client;
    }

    // GET /games/info/v2?id=...  ->  desarrolladoras del juego
    public List<ITADCompany> findDevelopersByGameId(String itadGameId) {
        return ApiConnect(() -> {
            ITADGameCompaniesResponse response = client.get()
                    .uri(uri -> uri.path("/games/info/v2")
                            .queryParam("id", itadGameId)
                            .build())
                    .retrieve()
                    .body(ITADGameCompaniesResponse.class);
            return response == null || response.developers() == null
                    ? List.<ITADCompany>of()
                    : response.developers();
        }, List.of());
    }
}
