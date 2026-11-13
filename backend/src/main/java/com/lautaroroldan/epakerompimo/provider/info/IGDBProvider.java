package com.lautaroroldan.epakerompimo.provider.info;

import com.lautaroroldan.epakerompimo.client.AbstractApiClient;
import com.lautaroroldan.epakerompimo.dto.game.CompanyDTO;
import com.lautaroroldan.epakerompimo.dto.game.CompanyDetailDTO;
import com.lautaroroldan.epakerompimo.dto.game.CompanyGameDTO;
import com.lautaroroldan.epakerompimo.dto.game.CompanyGamesDTO;
import com.lautaroroldan.epakerompimo.dto.game.GameDetailDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Game metadata adapter backed by IGDB.
 * <p>
 * IGDB is queried with {@code POST} and an Apicalypse body, not with query
 * parameters. Every request is limited to the fields declared in
 * {@link #SEARCH_FIELDS}, {@link #DETAIL_FIELDS} or {@link #COMPANY_GAME_FIELDS}.
 */
@Component
public class IGDBProvider extends AbstractApiClient implements GameInfoProvider {

    private static final String SEARCH_FIELDS =
            "name,slug,summary,storyline,first_release_date,cover.url,platforms.name";

    /**
     * Media and tags are only requested for the profile view: expanding them
     * for ten search results would multiply the payload for data nobody shows.
     * Credits are expanded too, so the response carries company names and
     * roles instead of the raw involved_companies row ids.
     */
    private static final String DETAIL_FIELDS = SEARCH_FIELDS
            + ",genres.name,themes.name,keywords.name"
            + ",screenshots.url,videos.video_id,videos.name"
            + ",involved_companies.id,involved_companies.company.id,involved_companies.company.name"
            + ",involved_companies.developer,involved_companies.publisher";

    private static final String COMPANY_FIELDS =
            "id,name,slug,description,start_date,logo.url,websites.url";

    private static final String COMPANY_GAME_FIELDS =
            "id,name,first_release_date,cover.url";

    /** Enough to describe a game without turning the UI into a wall of chips. */
    private static final int MAX_TAGS = 12;

    /**
     * IGDB ranks recent releases and bundles above the canonical title, so an
     * exact match can sit far down the list ("assassin's creed" surfaces the
     * original at position 20 of 45). A wide window lets the detail flow pick
     * the exact title instead of whatever IGDB returned first.
     */
    private static final int SEARCH_LIMIT = 50;

    private static final MediaType APICALYPSE = new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8);

    private final RestClient client;

    public IGDBProvider(@Qualifier("IGDBClient") RestClient client) {
        this.client = client;
    }

    @Override
    public List<GameDetailDTO> searchGame(String title) {
        String query = "fields " + SEARCH_FIELDS + ";"
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

        String query = "fields " + DETAIL_FIELDS + "; where id = " + gameId + "; limit 1;";

        return ApiConnect(() -> queryGames(query).stream()
                .findFirst()
                .map(this::toGameDetailDTO)
                .orElse(null), null);
    }

    @Override
    public CompanyDetailDTO findCompanyById(long companyId) {
        String query = "fields " + COMPANY_FIELDS + "; where id = " + companyId + "; limit 1;";

        return ApiConnect(() -> queryCompanies(query).stream()
                .findFirst()
                .map(this::toCompanyDetailDTO)
                .orElse(null), null);
    }

    @Override
    public CompanyGamesDTO findGamesByCompany(long companyId, int offset, int limit) {
        // version_parent and parent_game keep editions, DLC and remasters out:
        // a company page should list the games, not every SKU of them.
        String query = "fields " + COMPANY_GAME_FIELDS + ";"
                + " where involved_companies.company = " + companyId
                + " & version_parent = null & parent_game = null;"
                + " sort first_release_date desc;"
                + " limit " + limit + "; offset " + offset + ";";

        return ApiConnect(() -> {
            ResponseEntity<List<IGDBGameResponse>> response = client.post()
                    .uri("/games")
                    .contentType(APICALYPSE)
                    .body(query)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<List<IGDBGameResponse>>() {
                    });

            List<IGDBGameResponse> body = response.getBody();
            List<CompanyGameDTO> games = body == null ? List.of() : body.stream()
                    .map(this::toCompanyGameDTO)
                    .toList();
            return new CompanyGamesDTO(totalOf(response), games);
        }, new CompanyGamesDTO(0, List.of()));
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

    private List<IGDBCompanyResponse> queryCompanies(String apicalypseBody) {
        return client.post()
                .uri("/companies")
                .contentType(APICALYPSE)
                .body(apicalypseBody)
                .retrieve()
                .body(new ParameterizedTypeReference<List<IGDBCompanyResponse>>() {
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
                platformNamesOf(response),
                screenshotUrlsOf(response),
                trailerUrlOf(response),
                tagsOf(response),
                companiesOf(response)
        );
    }

    private CompanyDetailDTO toCompanyDetailDTO(IGDBCompanyResponse response) {
        return new CompanyDetailDTO(
                response.id(),
                response.name(),
                response.slug(),
                response.description(),
                foundedYearOf(response),
                companyLogoUrlOf(response),
                websiteUrlOf(response)
        );
    }

    private CompanyGameDTO toCompanyGameDTO(IGDBGameResponse response) {
        return new CompanyGameDTO(response.id(), response.name(), coverUrlOf(response), releaseDateOf(response));
    }

    /**
     * Ranks the credited companies so the profile can show a single recognizable
     * brand: a company that both develops and publishes wins, then the first
     * publisher (Rockstar Games, Sony, Konami), then the first developer when
     * the game has no publisher. Support studios without a role are dropped.
     */
    private List<CompanyDTO> companiesOf(IGDBGameResponse response) {
        if (response.involved_companies() == null) {
            return List.of();
        }

        List<CompanyDTO> companies = response.involved_companies().stream()
                .filter(credit -> credit.company() != null && credit.company().name() != null)
                .filter(credit -> credit.developer() || credit.publisher())
                .sorted(Comparator.comparingLong(IGDBGameResponse.IGDBInvolvedCompany::id))
                .map(credit -> new CompanyDTO(credit.company().id(), credit.company().name(),
                        credit.developer(), credit.publisher()))
                .distinct()
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        Optional<CompanyDTO> primary = companies.stream()
                .filter(company -> company.developer() && company.publisher())
                .findFirst()
                .or(() -> companies.stream().filter(CompanyDTO::publisher).findFirst())
                .or(() -> companies.stream().filter(CompanyDTO::developer).findFirst());
        if (primary.isEmpty()) {
            return List.of();
        }

        companies.remove(primary.get());
        companies.add(0, primary.get());
        return companies;
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

    private Integer foundedYearOf(IGDBCompanyResponse response) {
        Long epochSeconds = response.start_date();
        if (epochSeconds == null) {
            return null;
        }
        return LocalDate.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC).getYear();
    }

    /** IGDB image URLs are protocol-relative and default to a 90x128 thumbnail. */
    private String coverUrlOf(IGDBGameResponse response) {
        if (response.cover() == null || response.cover().url() == null) {
            return null;
        }
        String url = response.cover().url().replace("t_thumb", "t_cover_big");
        return absoluteUrl(url);
    }

    /** Company logos ship as 90x90 thumbs; the profile header wants the logo size. */
    private String companyLogoUrlOf(IGDBCompanyResponse response) {
        if (response.logo() == null || response.logo().url() == null) {
            return null;
        }
        String url = response.logo().url().replace("t_thumb", "t_logo_med");
        return absoluteUrl(url);
    }

    private String websiteUrlOf(IGDBCompanyResponse response) {
        if (response.websites() == null) {
            return null;
        }
        return response.websites().stream()
                .map(IGDBCompanyResponse.IGDBWebsite::url)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /** IGDB reports the size of the whole result set in X-Count, paging included. */
    private static int totalOf(ResponseEntity<?> response) {
        String header = response.getHeaders().getFirst("X-Count");
        if (header == null) {
            return 0;
        }
        try {
            return Integer.parseInt(header.trim());
        } catch (NumberFormatException e) {
            log.warn("IGDB returned a non numeric X-Count: {}", header);
            return 0;
        }
    }

    private List<String> platformNamesOf(IGDBGameResponse response) {
        return namesOf(response.platforms());
    }

    /**
     * IGDB splits what RAWG called "tags" across three entities. Genres and
     * themes are curated and go first; keywords add the specific ones
     * ("souls-like", "open world") and are capped to keep the list useful.
     */
    private List<String> tagsOf(IGDBGameResponse response) {
        List<String> tags = new ArrayList<>();
        tags.addAll(namesOf(response.genres()));
        tags.addAll(namesOf(response.themes()));
        tags.addAll(namesOf(response.keywords()));
        return tags.stream()
                .distinct()
                .limit(MAX_TAGS)
                .toList();
    }

    private List<String> namesOf(List<IGDBGameResponse.IGDBNamed> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(IGDBGameResponse.IGDBNamed::name)
                .filter(name -> name != null && !name.isBlank())
                .toList();
    }

    /** IGDB serves screenshots as 90x90 thumbnails; the gallery wants 889x500. */
    private List<String> screenshotUrlsOf(IGDBGameResponse response) {
        if (response.screenshots() == null) {
            return List.of();
        }
        return response.screenshots().stream()
                .map(IGDBGameResponse.IGDBScreenshot::url)
                .filter(Objects::nonNull)
                .map(url -> url.replace("t_thumb", "t_screenshot_big"))
                .map(IGDBProvider::absoluteUrl)
                .toList();
    }

    /** IGDB stores YouTube video ids, so the contract exposes an embeddable URL. */
    private String trailerUrlOf(IGDBGameResponse response) {
        if (response.videos() == null) {
            return null;
        }
        return response.videos().stream()
                .map(IGDBGameResponse.IGDBVideo::video_id)
                .filter(id -> id != null && !id.isBlank())
                .findFirst()
                .map(id -> "https://www.youtube.com/embed/" + id)
                .orElse(null);
    }

    private static String absoluteUrl(String url) {
        return url.startsWith("//") ? "https:" + url : url;
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
