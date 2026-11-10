package com.lautaroroldan.epakerompimo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ApiClientConfig {

    @Bean("ITADClient")
    public RestClient itadClient(RestClient.Builder builder, @Value("${itad.api.key}") String apiKey) {
        return builder
                .baseUrl("https://api.isthereanydeal.com/")
                .defaultHeader("ITAD-API-Key", apiKey)
                .build();
    }

    /**
     * IGDB requires a static {@code Client-ID} header plus a rotating
     * {@code Authorization: Bearer ...} token, so the token is resolved on every
     * request through the interceptor instead of a fixed default header.
     */
    @Bean("IGDBClient")
    public RestClient igdbClient(
            RestClient.Builder builder,
            IgdbTokenService tokenService,
            @Value("${igdb.client.id}") String clientId
    ) {
        return builder.clone()
                .baseUrl("https://api.igdb.com/v4")
                .defaultHeader("Client-ID", clientId)
                .requestInterceptor((request, body, execution) -> {
                    request.getHeaders().setBearerAuth(tokenService.accessToken());
                    return execution.execute(request, body);
                })
                .build();
    }
}
