package com.lautaroroldan.epakerompimo.client;

import com.lautaroroldan.epakerompimo.provider.info.IGDBCompanyResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Consulta empresas en IGDB: busca desarrolladoras por nombre, trae una empresa por id y arma la URL
 * de su logo.
 * <p>
 * Usa el mismo cliente que {@code IGDBProvider} ("IGDBClient"), que ya manda el
 * Client-ID y el token de Twitch. Todo es opcional: si IGDB falla, no encuentra la
 * empresa o la empresa no tiene logo, devuelve {@code null} o una lista vacía y nunca rompe quien lo llama.
 */
@Component
public class IGDBCompanyClient extends AbstractApiClient {

    private static final String LOGO_URL = "https://images.igdb.com/igdb/image/upload/t_logo_med/%s.png";
    private static final MediaType APICALYPSE = new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8);

    // Se piden más de las que se muestran para poder ordenarlas mejor (ver searchDevelopers)
    private static final int SEARCH_FETCH_LIMIT = 20;
    private static final int SEARCH_RESULTS = 8;

    private final RestClient client;

    public IGDBCompanyClient(@Qualifier("IGDBClient") RestClient client) {
        this.client = client;
    }

    /**
     * Desarrolladoras cuyo nombre CONTIENE el texto buscado (solo empresas que desarrollaron algún juego).
     * Primero van las que empiezan con ese texto y después las de nombre más corto.
     */
    public List<IGDBCompanyResponse> searchDevelopers(String name) {
        String clean = name == null ? "" : sanitize(name);
        if (clean.length() < 2) {
            return List.of();
        }

        try {
            String lower = clean.toLowerCase(Locale.ROOT);
            return queryCompanies("name ~ *\"" + clean + "\"* & developed != null", SEARCH_FETCH_LIMIT).stream()
                    .filter(c -> c.name() != null)
                    .sorted(Comparator
                            .comparing((IGDBCompanyResponse c) -> !c.name().toLowerCase(Locale.ROOT).startsWith(lower))
                            .thenComparingInt(c -> c.name().length()))
                    .limit(SEARCH_RESULTS)
                    .toList();
        } catch (RuntimeException e) {
            log.warn("No se pudo buscar empresas '{}' en IGDB: {}", name, e.getMessage());
            return List.of();
        }
    }

    /** Una empresa por su id de IGDB (devuelve nombre y logo). null si no existe o IGDB falla. */
    public IGDBCompanyResponse findById(long id) {
        try {
            return queryCompanies("id = " + id, 1).stream().findFirst().orElse(null);
        } catch (RuntimeException e) {
            log.warn("No se pudo obtener la empresa {} en IGDB: {}", id, e.getMessage());
            return null;
        }
    }

    /** URL del logo de una empresa, o null si no tiene. */
    public String logoUrlOf(IGDBCompanyResponse company) {
        return hasLogo(company) ? LOGO_URL.formatted(company.logo().image_id()) : null;
    }

    private boolean hasLogo(IGDBCompanyResponse c) {
        return c != null && c.logo() != null && c.logo().image_id() != null && !c.logo().image_id().isBlank();
    }

    private List<IGDBCompanyResponse> queryCompanies(String where, int limit) {
        String query = "fields name,logo.image_id; where " + where + "; limit " + limit + ";";
        List<IGDBCompanyResponse> response = client.post()
                .uri("/companies")
                .contentType(APICALYPSE)
                .body(query)
                .retrieve()
                .body(new ParameterizedTypeReference<List<IGDBCompanyResponse>>() {
                });
        return response == null ? List.of() : response;
    }

    /** El nombre se interpola en una consulta Apicalypse: se sacan comillas, ; \ y * para no romperla. */
    private static String sanitize(String name) {
        return name.replace("\\", "")
                .replace("\"", "")
                .replace(";", "")
                .replace("*", "")
                .trim();
    }
}
