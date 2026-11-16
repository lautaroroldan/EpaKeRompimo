package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.GameDTO;
import com.lautaroroldan.epakerompimo.dto.PriceDTO;
import com.lautaroroldan.epakerompimo.dto.StoreDTO;
import com.lautaroroldan.epakerompimo.entity.GameMatchOverride;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameSearchServiceManagerTest {

    @Mock
    private GameCatalogService gameCatalogService;
    @Mock
    private PriceAggregationService priceAggregationService;
    @Mock
    private GameDetailService gameDetailService;
    @Mock
    private GameMatchOverrideService matchOverrideService;

    @InjectMocks
    private GameSearchServiceManager service;

    @Test
    void usesTheFixedItadGameInsteadOfTheAutomaticMatch() {
        when(matchOverrideService.findGameOverride("Elden Ring"))
                .thenReturn(Optional.of(fixedGame("itad-fixed")));
        when(priceAggregationService.pricesByGameId(any(String[].class), eq("AR")))
                .thenReturn(Map.of("itad-fixed", List.of(offer(10))));
        when(matchOverrideService.applyStoreOverrides(eq("Elden Ring"), anyList()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        List<PriceDTO> result = service.findOffersByTitle("Elden Ring", "AR");

        assertThat(result).hasSize(1);
        verify(gameCatalogService, never()).searchByTitle(anyString());
        verify(matchOverrideService).applyStoreOverrides(eq("Elden Ring"), anyList());
    }

    @Test
    void fallsBackToTheExactTitleWhenThereIsNoOverride() {
        when(matchOverrideService.findGameOverride("Elden Ring")).thenReturn(Optional.empty());
        when(gameCatalogService.searchByTitle("Elden Ring")).thenReturn(List.of(
                game("nightreign", "Elden Ring Nightreign"),
                game("elden", "Elden Ring")));
        when(priceAggregationService.pricesByGameId(any(String[].class), eq("AR")))
                .thenReturn(Map.of("elden", List.of(offer(20))));
        when(matchOverrideService.applyStoreOverrides(eq("Elden Ring"), anyList()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        service.findOffersByTitle("Elden Ring", "AR");

        ArgumentCaptor<String[]> ids = ArgumentCaptor.forClass(String[].class);
        verify(priceAggregationService).pricesByGameId(ids.capture(), eq("AR"));
        assertThat(ids.getValue()).containsExactly("elden");
    }

    private static GameMatchOverride fixedGame(String itadId) {
        GameMatchOverride override = new GameMatchOverride();
        override.setTitleKey("elden ring");
        override.setItadGameId(itadId);
        return override;
    }

    private static GameDTO game(String id, String title) {
        return new GameDTO(id, title, "", "game", "", "", new String[]{});
    }

    private static PriceDTO offer(int price) {
        return new PriceDTO(BigDecimal.valueOf(price), BigDecimal.valueOf(price),
                new StoreDTO("Steam", "https://store.example", ""), "ITAD", 0, "USD");
    }
}
