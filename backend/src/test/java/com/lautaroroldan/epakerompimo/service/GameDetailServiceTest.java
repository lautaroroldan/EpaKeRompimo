package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.game.GameDetailDTO;
import com.lautaroroldan.epakerompimo.entity.GameMatchOverride;
import com.lautaroroldan.epakerompimo.provider.info.GameInfoProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameDetailServiceTest {

    @Mock
    private GameInfoProvider gameInfoProvider;
    @Mock
    private GameMatchOverrideService matchOverrideService;

    @InjectMocks
    private GameDetailService service;

    @Test
    void usesTheFixedIgdbGameWithoutSearching() {
        when(matchOverrideService.findGameOverride("Elden Ring"))
                .thenReturn(Optional.of(fixedGame(42L, null)));
        GameDetailDTO fixed = detail(42L, "Elden Ring");
        when(gameInfoProvider.findGameById("42")).thenReturn(fixed);

        assertThat(service.findByTitle("Elden Ring")).isSameAs(fixed);
        verify(gameInfoProvider, never()).searchGame(anyString());
    }

    @Test
    void fallsBackToAutomaticMatchingWhenTheFixedIdNoLongerExists() {
        when(matchOverrideService.findGameOverride("Elden Ring"))
                .thenReturn(Optional.of(fixedGame(999L, null)));
        when(gameInfoProvider.findGameById("999")).thenReturn(null);
        when(gameInfoProvider.searchGame("Elden Ring"))
                .thenReturn(List.of(detail(7L, "Elden Ring")));
        when(gameInfoProvider.findGameById("7")).thenReturn(detail(7L, "Elden Ring"));

        GameDetailDTO result = service.findByTitle("Elden Ring");

        assertThat(result).isNotNull();
        assertThat(result.externalId()).isEqualTo(7L);
    }

    @Test
    void prefersTheExactTitleAmongTheCandidates() {
        when(matchOverrideService.findGameOverride("Elden Ring")).thenReturn(Optional.empty());
        when(gameInfoProvider.searchGame("Elden Ring")).thenReturn(List.of(
                detail(1L, "Elden Ring Nightreign"),
                detail(2L, "Elden Ring")));
        when(gameInfoProvider.findGameById("2")).thenReturn(detail(2L, "Elden Ring"));

        assertThat(service.findByTitle("Elden Ring").externalId()).isEqualTo(2L);
    }

    private static GameMatchOverride fixedGame(Long igdbId, String itadId) {
        GameMatchOverride override = new GameMatchOverride();
        override.setTitleKey("elden ring");
        override.setIgdbGameId(igdbId);
        override.setItadGameId(itadId);
        return override;
    }

    private static GameDetailDTO detail(long id, String title) {
        return new GameDetailDTO(id, "slug", title, "desc", "2022-02-25", "", List.of(),
                List.of(), null, List.of(), List.of());
    }
}
