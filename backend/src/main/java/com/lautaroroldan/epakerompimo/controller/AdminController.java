package com.lautaroroldan.epakerompimo.controller;

import com.lautaroroldan.epakerompimo.dto.GameDTO;
import com.lautaroroldan.epakerompimo.dto.PriceDTO;
import com.lautaroroldan.epakerompimo.dto.admin.MatchOverrideResponse;
import com.lautaroroldan.epakerompimo.dto.admin.StoreLinkOverrideResponse;
import com.lautaroroldan.epakerompimo.dto.game.GameDetailDTO;
import com.lautaroroldan.epakerompimo.dto.report.MatchReportResolveRequest;
import com.lautaroroldan.epakerompimo.dto.report.MatchReportResponse;
import com.lautaroroldan.epakerompimo.entity.GameMatchOverride;
import com.lautaroroldan.epakerompimo.entity.StoreLinkOverride;
import com.lautaroroldan.epakerompimo.enums.MatchReportStatus;
import com.lautaroroldan.epakerompimo.enums.MatchReportType;
import com.lautaroroldan.epakerompimo.security.AuthUtils;
import com.lautaroroldan.epakerompimo.service.GameMatchOverrideService;
import com.lautaroroldan.epakerompimo.service.GameSearchService;
import com.lautaroroldan.epakerompimo.service.MatchReportService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Panel del administrador. Todas las rutas quedan protegidas por
 * {@code hasRole('ADMIN')} en {@link com.lautaroroldan.epakerompimo.config.SecurityConfig}.
 */
@Validated
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final MatchReportService matchReportService;
    private final GameSearchService gameSearchService;
    private final GameMatchOverrideService matchOverrideService;
    private final String defaultCountry;

    public AdminController(MatchReportService matchReportService,
                           GameSearchService gameSearchService,
                           GameMatchOverrideService matchOverrideService,
                           @Value("${itad.country:US}") String defaultCountry) {
        this.matchReportService = matchReportService;
        this.gameSearchService = gameSearchService;
        this.matchOverrideService = matchOverrideService;
        this.defaultCountry = defaultCountry;
    }

    // ---------- Reportes ----------

    @GetMapping("/reports")
    public List<MatchReportResponse> reports(@RequestParam(required = false) MatchReportStatus status,
                                             @RequestParam(required = false) MatchReportType type) {
        return matchReportService.findAll(status, type);
    }

    @GetMapping("/reports/{id}")
    public MatchReportResponse report(@PathVariable Long id) {
        return matchReportService.findById(id);
    }

    @PatchMapping("/reports/{id}")
    public MatchReportResponse resolve(@PathVariable Long id,
                                       @Valid @RequestBody MatchReportResolveRequest request,
                                       Authentication authentication) {
        return matchReportService.resolve(id, request, AuthUtils.userId(authentication));
    }

    // ---------- Candidatos para arreglar el matcheo ----------

    @GetMapping("/candidates/igdb")
    public List<GameDetailDTO> igdbCandidates(@RequestParam @NotBlank String title) {
        return gameSearchService.findGameCandidates(title);
    }

    @GetMapping("/candidates/itad")
    public List<GameDTO> itadCandidates(@RequestParam @NotBlank String title) {
        return gameSearchService.findCatalogCandidates(title);
    }

    /** Ofertas reales de un candidato de ITAD, para revisar los links antes de fijarlo. */
    @GetMapping("/candidates/itad/{itadGameId}/prices")
    public List<PriceDTO> itadPrices(@PathVariable String itadGameId,
                                     @RequestParam(required = false) String country) {
        String targetCountry = country == null || country.isBlank() ? defaultCountry : country;
        return gameSearchService.findOffersByItadId(itadGameId, targetCountry);
    }

    // ---------- Correcciones vigentes ----------

    @GetMapping("/overrides")
    public MatchOverrideResponse overrides(@RequestParam @NotBlank String title) {
        GameMatchOverride gameOverride = matchOverrideService.findGameOverride(title).orElse(null);
        List<StoreLinkOverride> links = matchOverrideService.findStoreOverrides(title);

        String displayTitle = gameOverride != null && gameOverride.getDisplayTitle() != null
                ? gameOverride.getDisplayTitle()
                : links.stream()
                        .map(StoreLinkOverride::getDisplayTitle)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(title);

        Instant updatedAt = gameOverride != null ? gameOverride.getUpdatedAt() : null;
        for (StoreLinkOverride link : links) {
            if (link.getUpdatedAt() != null && (updatedAt == null || link.getUpdatedAt().isAfter(updatedAt))) {
                updatedAt = link.getUpdatedAt();
            }
        }

        List<StoreLinkOverrideResponse> storeLinks = links.stream()
                .map(link -> new StoreLinkOverrideResponse(
                        link.getStoreName(), link.getUrl(), link.isHidden(), link.getUpdatedAt()))
                .toList();

        return new MatchOverrideResponse(
                displayTitle,
                gameOverride != null ? gameOverride.getIgdbGameId() : null,
                gameOverride != null ? gameOverride.getItadGameId() : null,
                updatedAt,
                storeLinks);
    }

    @DeleteMapping("/overrides")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearOverrides(@RequestParam @NotBlank String title) {
        matchOverrideService.clear(title);
    }
}
