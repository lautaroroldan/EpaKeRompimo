package com.lautaroroldan.epakerompimo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Configuration
public class ApiClientConfig {

    @Bean("cheapSharkClient")
    public RestClient cheapSharkClient(RestClient.Builder builder) {
        return builder
                .baseUrl("https://www.cheapshark.com/api/1.0")
                .defaultHeader(HttpHeaders.USER_AGENT, "EpaKeRompimo/1.0 (lautaronicolasroldan@gmail.com)")
                .build();
    }

    @Bean("ITADClient")
    public RestClient itadClient(RestClient.Builder builder, @Value("${itad.api.key}") String apiKey) {
        return builder
                .baseUrl("https://api.isthereanydeal.com/")
                .defaultHeader("ITAD-API-Key", apiKey)
                .build();
    }

    @Bean("rawgClient")
    public RestClient rawgClient(
            RestClient.Builder builder,
            @Value("${rawg.api.key}") String apiKey
    ) {
        return builder
                .baseUrl("https://api.rawg.io/api")
                .defaultUriVariables(Map.of("key", apiKey))
                .build();
    }
}
