package com.lautaroroldan.epakerompimo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;

/**
 * Obtains and caches the Twitch OAuth2 access token required by the IGDB API.
 * <p>
 * IGDB authenticates through Twitch's client-credentials flow: a single
 * application token is reused until shortly before it expires. Twitch tokens
 * typically last around 60 days, so this refresh happens rarely.
 */
@Component
public class IgdbTokenService {

    private static final Logger log = LoggerFactory.getLogger(IgdbTokenService.class);

    /** Refresh the token this many seconds before it actually expires. */
    private static final long REFRESH_MARGIN_SECONDS = 120;

    private final RestClient twitchClient;
    private final String clientId;
    private final String clientSecret;

    private volatile String accessToken;
    private volatile Instant expiresAt = Instant.EPOCH;

    public IgdbTokenService(RestClient.Builder builder,
                            @Value("${igdb.client.id}") String clientId,
                            @Value("${igdb.client.secret}") String clientSecret) {
        this.twitchClient = builder.clone().baseUrl("https://id.twitch.tv/oauth2").build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String accessToken() {
        if (needsRefresh()) {
            refresh();
        }
        return accessToken;
    }

    private boolean needsRefresh() {
        return accessToken == null || Instant.now().isAfter(expiresAt.minusSeconds(REFRESH_MARGIN_SECONDS));
    }

    private synchronized void refresh() {
        // Another thread may have refreshed while this one waited for the lock.
        if (!needsRefresh()) {
            return;
        }
        if (clientId.isBlank() || clientSecret.isBlank()) {
            throw new IllegalStateException(
                    "IGDB credentials are missing: set IGDB_CLIENT_ID and IGDB_CLIENT_SECRET");
        }

        TwitchTokenResponse response = twitchClient.post()
                .uri(uri -> uri.path("/token")
                        .queryParam("client_id", clientId)
                        .queryParam("client_secret", clientSecret)
                        .queryParam("grant_type", "client_credentials")
                        .build())
                .retrieve()
                .body(TwitchTokenResponse.class);

        this.accessToken = response.access_token();
        this.expiresAt = Instant.now().plusSeconds(response.expires_in());
        log.info("IGDB access token refreshed (expires in {} seconds)", response.expires_in());
    }

    /** Twitch responds with snake_case keys, mirrored here on purpose. */
    public record TwitchTokenResponse(String access_token, long expires_in, String token_type) {
    }
}
