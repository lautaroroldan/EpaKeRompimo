package com.lautaroroldan.epakerompimo.provider.info;

import com.lautaroroldan.epakerompimo.client.AbstractApiClient;
import com.lautaroroldan.epakerompimo.dto.game.GameDetailDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * Game metadata adapter backed by IGDB.
 * <p>
 * IGDB is queried with {@code POST} and an Apicalypse body, not with query
 * parameters. Every request is limited to the fields declared in
 * {@link #FIELDS}.
 */
@Component
public class IGDBProvider extends AbstractApiClient implements GameInfoProvider {

    private static final String FIELDS =
            "name,slug,summary,storyline,first_release_date,cover.url,platforms.name";

    private static final int SEARCH_LIMIT = 10;

    private static final MediaType APICALYPSE = new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8);

    private final RestClient client;

    public IGDBProvider(@Qualifier("IGDBClient") RestClient client) {
        this.client = client;
    }

    @Override
    public List<GameDetailDTO> searchGame(String title) {
        String query = "fields " + FIELDS + ";"
                + " search \"" + sanitize(title) + "\";"
                + " where version_parent = null;"
                + " limit " + SEARCH_LIMIT + ";";

        return ApiConnect(() -> queryGames(query).stream()
                .map(this::toGameDetailDTO)
                .toList(), new ArrayList<>());
    }

    @Override
    public GameDetailDTO findGameById(String id) {
        Long gameId = parseId(id);
        if (gameId == null) {
            return null;
        }

        String query = "fields " + FIELDS + "; where id = " + gameId + "; limit 1;";

        return ApiConnect(() -> queryGames(query).stream()
                .findFirst()
                .map(this::toGameDetailDTO)
                .orElse(null), null);
    }

    private List<IGDBGameResponse> queryGames(String apicalypseBody) {
        return client.post()
                .uri("/games")
                .contentType(APICALYPSE)
                .body(apicalypseBody)
                .retrieve()
                .body(new ParameterizedTypeReference<List<IGDBGameResponse>>() {
                });
    }

    private GameDetailDTO toGameDetailDTO(IGDBGameResponse response) {
        return new GameDetailDTO(
                response.id(),
                response.slug(),
                response.name(),
                descriptionOf(response),
                releaseDateOf(response),
                coverUrlOf(response),
                platformNamesOf(response)
        );
    }

    private String descriptionOf(IGDBGameResponse response) {
        if (response.summary() != null && !response.summary().isBlank()) {
            return response.summary();
        }
        return response.storyline();
    }

    /** IGDB sends a Unix timestamp in seconds; the contract exposes an ISO date. */
    private String releaseDateOf(IGDBGameResponse response) {
        Long epochSeconds = response.first_release_date();
        if (epochSeconds == null) {
            return null;
        }
        return LocalDate.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC).toString();
    }

    /** IGDB image URLs are protocol-relative and default to a 90x128 thumbnail. */
    private String coverUrlOf(IGDBGameResponse response) {
        if (response.cover() == null || response.cover().url() == null) {
            return null;
        }
        String url = response.cover().url().replace("t_thumb", "t_cover_big");
        return url.startsWith("//") ? "https:" + url : url;
    }

    private List<String> platformNamesOf(IGDBGameResponse response) {
        if (response.platforms() == null) {
            return List.of();
        }
        return response.platforms().stream()
                .map(IGDBGameResponse.IGDBPlatform::name)
                .filter(name -> name != null && !name.isBlank())
                .toList();
    }

    /**
     * The title is interpolated into an Apicalypse query, so quotes, semicolons
     * and backslashes have to be stripped to keep the query well formed.
     */
    private static String sanitize(String title) {
        return title.replace("\\", "")
                .replace("\"", "")
                .replace(";", "")
                .trim();
    }

    private static Long parseId(String id) {
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException e) {
            log.warn("IGDB id is not numeric: {}", id);
            return null;
        }
    }
}
