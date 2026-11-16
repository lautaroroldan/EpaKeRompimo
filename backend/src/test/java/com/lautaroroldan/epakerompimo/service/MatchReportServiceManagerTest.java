package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.report.MatchReportRequest;
import com.lautaroroldan.epakerompimo.dto.report.MatchReportResolveRequest;
import com.lautaroroldan.epakerompimo.dto.report.MatchReportResponse;
import com.lautaroroldan.epakerompimo.entity.GameMatchOverride;
import com.lautaroroldan.epakerompimo.entity.MatchReport;
import com.lautaroroldan.epakerompimo.entity.User;
import com.lautaroroldan.epakerompimo.enums.MatchReportStatus;
import com.lautaroroldan.epakerompimo.enums.MatchReportType;
import com.lautaroroldan.epakerompimo.exceptions.InvalidMatchReportException;
import com.lautaroroldan.epakerompimo.exceptions.MatchReportNotFoundException;
import com.lautaroroldan.epakerompimo.repository.MatchReportRepository;
import com.lautaroroldan.epakerompimo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchReportServiceManagerTest {

    @Mock
    private MatchReportRepository matchReportRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GameMatchOverrideService matchOverrideService;

    @InjectMocks
    private MatchReportServiceManager service;

    @Test
    void createsAPendingReportWithTheSnapshot() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user(5L, "pepe")));
        when(matchReportRepository.save(any(MatchReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MatchReportResponse response = service.create(5L, new MatchReportRequest(
                MatchReportType.DESCRIPTION_MISMATCH, "  Elden Ring  ", 42L, "itad-1",
                null, null, "  es la secuela  "));

        assertThat(response.status()).isEqualTo(MatchReportStatus.PENDING);
        assertThat(response.gameTitle()).isEqualTo("Elden Ring");
        assertThat(response.message()).isEqualTo("es la secuela");
        assertThat(response.username()).isEqualTo("pepe");
        assertThat(response.createdAt()).isNotNull();
    }

    @Test
    void requiresAStoreWhenTheLinkIsReported() {
        assertThatThrownBy(() -> service.create(5L, new MatchReportRequest(
                MatchReportType.STORE_LINK_MISMATCH, "Elden Ring", null, null, "  ", null, null)))
                .isInstanceOf(InvalidMatchReportException.class);

        verify(userRepository, never()).findById(any());
    }

    @Test
    void resolvingSavesTheFixedGameAndTheStoreLink() {
        MatchReport report = pendingReport();
        when(matchReportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(userRepository.findById(9L)).thenReturn(Optional.of(user(9L, "admin")));

        MatchReportResponse response = service.resolve(1L, new MatchReportResolveRequest(
                MatchReportStatus.RESOLVED, "  era Nightreign  ", 42L, "itad-correcto",
                "Steam", "https://store.steampowered.com/app/1245620", false), 9L);

        verify(matchOverrideService).saveGameOverride("Elden Ring", 42L, "itad-correcto", 9L);
        verify(matchOverrideService).saveStoreOverride(
                "Elden Ring", "Steam", "https://store.steampowered.com/app/1245620", false, 9L);
        assertThat(response.status()).isEqualTo(MatchReportStatus.RESOLVED);
        assertThat(response.adminNote()).isEqualTo("era Nightreign");
        assertThat(response.resolvedBy()).isEqualTo("admin");
        assertThat(response.resolvedAt()).isNotNull();
    }

    @Test
    void keepsTheAlreadyPinnedProviderWhenOnlyOneIsChanged() {
        MatchReport report = pendingReport();
        when(matchReportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(userRepository.findById(9L)).thenReturn(Optional.of(user(9L, "admin")));

        GameMatchOverride existing = new GameMatchOverride();
        existing.setIgdbGameId(42L);
        existing.setItadGameId("itad-viejo");
        when(matchOverrideService.findGameOverride("Elden Ring")).thenReturn(Optional.of(existing));

        service.resolve(1L, new MatchReportResolveRequest(MatchReportStatus.RESOLVED, null,
                null, "itad-nuevo", null, null, null), 9L);

        verify(matchOverrideService).saveGameOverride("Elden Ring", 42L, "itad-nuevo", 9L);
    }

    @Test
    void rejectingDoesNotTouchTheMatcher() {
        when(matchReportRepository.findById(1L)).thenReturn(Optional.of(pendingReport()));
        when(userRepository.findById(9L)).thenReturn(Optional.of(user(9L, "admin")));

        MatchReportResponse response = service.resolve(1L,
                new MatchReportResolveRequest(MatchReportStatus.REJECTED, "estaba bien", null, null,
                        null, null, null), 9L);

        verify(matchOverrideService, never()).saveGameOverride(any(), any(), any(), any());
        verify(matchOverrideService, never()).saveStoreOverride(any(), any(), any(), anyBoolean(), any());
        assertThat(response.status()).isEqualTo(MatchReportStatus.REJECTED);
    }

    @Test
    void onlyResolvedOrRejectedAreAccepted() {
        assertThatThrownBy(() -> service.resolve(1L, new MatchReportResolveRequest(
                MatchReportStatus.PENDING, null, null, null, null, null, null), 9L))
                .isInstanceOf(InvalidMatchReportException.class);
    }

    @Test
    void unknownReportIsNotFound() {
        when(matchReportRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(404L))
                .isInstanceOf(MatchReportNotFoundException.class);
    }

    private static MatchReport pendingReport() {
        MatchReport report = new MatchReport();
        report.setId(1L);
        report.setUser(user(5L, "pepe"));
        report.setType(MatchReportType.STORE_LINK_MISMATCH);
        report.setStatus(MatchReportStatus.PENDING);
        report.setGameTitle("Elden Ring");
        report.setStoreName("Steam");
        report.setCurrentUrl("https://mal.example");
        return report;
    }

    private static User user(Long id, String username) {
        User user = new User();
        user.setIdUser(id);
        user.setUsername(username);
        return user;
    }
}
