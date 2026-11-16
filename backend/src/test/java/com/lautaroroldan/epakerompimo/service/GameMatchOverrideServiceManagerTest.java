package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.PriceDTO;
import com.lautaroroldan.epakerompimo.dto.StoreDTO;
import com.lautaroroldan.epakerompimo.entity.GameMatchOverride;
import com.lautaroroldan.epakerompimo.entity.StoreLinkOverride;
import com.lautaroroldan.epakerompimo.exceptions.InvalidMatchFixException;
import com.lautaroroldan.epakerompimo.repository.GameMatchOverrideRepository;
import com.lautaroroldan.epakerompimo.repository.StoreLinkOverrideRepository;
import com.lautaroroldan.epakerompimo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameMatchOverrideServiceManagerTest {

    @Mock
    private GameMatchOverrideRepository gameMatchOverrideRepository;
    @Mock
    private StoreLinkOverrideRepository storeLinkOverrideRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GameMatchOverrideServiceManager service;

    @Test
    void replacesCorrectedUrlsAndHidesOffers() {
        when(storeLinkOverrideRepository.findByTitleKey("elden ring")).thenReturn(List.of(
                storeLink("Steam", "https://store.steampowered.com/correcto", false),
                storeLink("GOG", null, true)));

        List<PriceDTO> offers = List.of(
                offer("Steam", "https://mal.example", 10),
                offer("GOG", "https://gog.example", 5),
                offer("Epic", "https://epic.example", 7));

        List<PriceDTO> result = service.applyStoreOverrides("Elden Ring", offers);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).store().urlStore())
                .isEqualTo("https://store.steampowered.com/correcto");
        assertThat(result.get(1).store().name()).isEqualTo("Epic");
    }

    @Test
    void leavesOffersUntouchedWhenThereAreNoOverrides() {
        when(storeLinkOverrideRepository.findByTitleKey("elden ring")).thenReturn(List.of());
        List<PriceDTO> offers = List.of(offer("Steam", "https://store.example", 10));

        assertThat(service.applyStoreOverrides("Elden Ring", offers)).isSameAs(offers);
    }

    @Test
    void storesTheFixedGameUnderTheNormalizedTitle() {
        when(gameMatchOverrideRepository.findByTitleKey("elden ring")).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(gameMatchOverrideRepository.save(any(GameMatchOverride.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.saveGameOverride("Elden Ring", 42L, "itad-123", 1L);

        ArgumentCaptor<GameMatchOverride> captor = ArgumentCaptor.forClass(GameMatchOverride.class);
        verify(gameMatchOverrideRepository).save(captor.capture());
        GameMatchOverride saved = captor.getValue();
        assertThat(saved.getTitleKey()).isEqualTo("elden ring");
        assertThat(saved.getDisplayTitle()).isEqualTo("Elden Ring");
        assertThat(saved.getIgdbGameId()).isEqualTo(42L);
        assertThat(saved.getItadGameId()).isEqualTo("itad-123");
    }

    @Test
    void rejectsAFixWithoutAnyTarget() {
        assertThatThrownBy(() -> service.saveGameOverride("Elden Ring", null, null, 1L))
                .isInstanceOf(InvalidMatchFixException.class);

        verify(gameMatchOverrideRepository, never()).save(any());
    }

    @Test
    void rejectsAStoreFixWithoutUrlOrHide() {
        assertThatThrownBy(() -> service.saveStoreOverride("Elden Ring", "Steam", "  ", false, 1L))
                .isInstanceOf(InvalidMatchFixException.class);
    }

    private static StoreLinkOverride storeLink(String store, String url, boolean hidden) {
        StoreLinkOverride override = new StoreLinkOverride();
        override.setTitleKey("elden ring");
        override.setStoreName(store);
        override.setUrl(url);
        override.setHidden(hidden);
        return override;
    }

    private static PriceDTO offer(String store, String url, int price) {
        return new PriceDTO(BigDecimal.valueOf(price), BigDecimal.valueOf(price),
                new StoreDTO(store, url, ""), "ITAD", 0, "USD");
    }
}
