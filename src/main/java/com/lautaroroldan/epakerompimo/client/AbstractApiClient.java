package com.lautaroroldan.epakerompimo.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;

import java.util.function.Supplier;

public abstract class AbstractApiClient {

    protected static final Logger log = LoggerFactory.getLogger(AbstractApiClient.class);

    protected <T> T ApiConnect(Supplier<T> llamada, T valorPorDefecto) {
        try {
            return llamada.get();
        } catch (RestClientException e) {
            log.warn("Fallo en llamada externa: {}", e.getMessage());
            return valorPorDefecto;
        }
    }
}
