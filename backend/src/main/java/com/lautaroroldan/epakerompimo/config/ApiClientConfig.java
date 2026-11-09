package com.lautaroroldan.epakerompimo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Configuration
public class ApiClientConfig {

    @Bean("ITADClient")
    public RestClient itadClient(RestClient.Builder builder, @Value("${itad.api.key}") String apiKey) {
        return builder
                .baseUrl("https://api.isthereanydeal.com/")
                .defaultHeader("ITAD-API-Key", apiKey)
                .build();
    }

    @Bean("RAWGClient")
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
