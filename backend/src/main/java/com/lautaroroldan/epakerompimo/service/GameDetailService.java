package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.game.GameDetailDTO;
import com.lautaroroldan.epakerompimo.entity.GameMatchOverride;
import com.lautaroroldan.epakerompimo.provider.info.GameInfoProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameDetailService {
    private final GameInfoProvider gameInfoProvider;
    private final GameMatchOverrideService matchOverrideService;

    public GameDetailService(GameInfoProvider gameInfoProvider,
                             GameMatchOverrideService matchOverrideService) {
        this.gameInfoProvider = gameInfoProvider;
        this.matchOverrideService = matchOverrideService;
    }

    public GameDetailDTO findByTitle(String title) {
        // Si el administrador fijó el juego correcto, manda esa decisión.
        GameDetailDTO fixed = findByOverride(title);
        if (fixed != null) {
            return fixed;
        }

        List<GameDetailDTO> candidates = gameInfoProvider.searchGame(title);
        GameDetailDTO candidate = preferExactTitle(candidates, title);
        if (candidate == null) {
            return null;
        }
        // Screenshots and videos are only expanded in the by-id lookup, so the
        // profile view resolves the match first and then fetches the full detail.
        GameDetailDTO detail = gameInfoProvider.findGameById(String.valueOf(candidate.externalId()));
        return detail != null ? detail : candidate;
    }

    /**
     * El id fijado puede haber desaparecido de IGDB; en ese caso se vuelve al
     * matcheo automático en lugar de romper la vista.
     */
    private GameDetailDTO findByOverride(String title) {
        GameMatchOverride override = matchOverrideService.findGameOverride(title).orElse(null);
        if (override == null || override.getIgdbGameId() == null) {
            return null;
        }
        return gameInfoProvider.findGameById(String.valueOf(override.getIgdbGameId()));
    }

    /**
     * IGDB ranks recent releases above the canonical one, so searching
     * "elden ring" returns "Elden Ring Nightreign" first. Prefer the candidate
     * whose title matches exactly once accents, case and punctuation are
     * ignored, and fall back to IGDB's own ordering when there is no exact hit.
     */
    private GameDetailDTO preferExactTitle(List<GameDetailDTO> candidates, String title) {
        GameDetailDTO exact = candidates.stream()
                .filter(candidate -> TitleNormalizer.sameTitle(candidate.title(), title))
                .findFirst()
                .orElse(null);
        return exact != null ? exact : candidates.stream().findFirst().orElse(null);
    }
}
