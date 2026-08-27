package com.lautaroroldan.EpaKeRompimo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ApiClientConfig {

    @Bean
    public RestClient cheapSharkClient(){
        return RestClient.builder()
                .baseUrl("https://www.cheapshark.com/api/1.0")
                .defaultHeader("User-Agent","EpaKeRompimo/1.0 (lautaronicolasroldan@gmail.com)")
                .build();
    }
}
