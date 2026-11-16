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
import com.lautaroroldan.epakerompimo.exceptions.UserNotFoundException;
import com.lautaroroldan.epakerompimo.repository.MatchReportRepository;
import com.lautaroroldan.epakerompimo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MatchReportServiceManager implements MatchReportService {

    private final MatchReportRepository matchReportRepository;
    private final UserRepository userRepository;
    private final GameMatchOverrideService matchOverrideService;

    public MatchReportServiceManager(MatchReportRepository matchReportRepository,
                                     UserRepository userRepository,
                                     GameMatchOverrideService matchOverrideService) {
        this.matchReportRepository = matchReportRepository;
        this.userRepository = userRepository;
        this.matchOverrideService = matchOverrideService;
    }

    @Override
    @Transactional
    public MatchReportResponse create(Long userId, MatchReportRequest request) {
        if (request.type() == MatchReportType.STORE_LINK_MISMATCH
                && (request.storeName() == null || request.storeName().isBlank())) {
            throw new InvalidMatchReportException(
                    "Para reportar un link tenés que indicar la tienda");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        MatchReport report = new MatchReport();
        report.setUser(user);
        report.setType(request.type());
        report.setStatus(MatchReportStatus.PENDING);
        report.setGameTitle(request.gameTitle().trim());
        report.setIgdbGameId(request.igdbGameId());
        report.setItadGameId(blankToNull(request.itadGameId()));
        report.setStoreName(blankToNull(request.storeName()));
        report.setCurrentUrl(blankToNull(request.currentUrl()));
        report.setMessage(blankToNull(request.message()));

        return toResponse(matchReportRepository.save(report));
    }

    @Override
    public List<MatchReportResponse> findMine(Long userId) {
        return matchReportRepository.findByUser_IdUserOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<MatchReportResponse> findAll(MatchReportStatus status, MatchReportType type) {
        List<MatchReport> reports;
        if (status != null && type != null) {
            reports = matchReportRepository.findByStatusAndTypeOrderByCreatedAtDesc(status, type);
        } else if (status != null) {
            reports = matchReportRepository.findByStatusOrderByCreatedAtDesc(status);
        } else if (type != null) {
            reports = matchReportRepository.findByTypeOrderByCreatedAtDesc(type);
        } else {
            reports = matchReportRepository.findAllByOrderByCreatedAtDesc();
        }
        return reports.stream().map(this::toResponse).toList();
    }

    @Override
    public MatchReportResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Override
    @Transactional
    public MatchReportResponse resolve(Long id, MatchReportResolveRequest request, Long adminUserId) {
        if (request.status() == null || request.status() == MatchReportStatus.PENDING) {
            throw new InvalidMatchReportException("El reporte solo se puede resolver o rechazar");
        }

        MatchReport report = getOrThrow(id);
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new UserNotFoundException(adminUserId));

        report.setAdminNote(blankToNull(request.adminNote()));
        report.setStatus(request.status());
        report.setResolvedAt(Instant.now());
        report.setResolvedBy(admin);

        if (request.status() == MatchReportStatus.RESOLVED) {
            applyFixes(report, request, adminUserId);
        }

        return toResponse(report);
    }

    /**
     * Guarda las correcciones permanentes. Solo se escribe lo que el admin
     * eligió: el panel manda los ids únicamente cuando cambió el match, y lo
     * que ya estaba fijado se conserva.
     */
    private void applyFixes(MatchReport report, MatchReportResolveRequest request, Long adminUserId) {
        boolean fixGame = request.igdbGameId() != null
                || (request.itadGameId() != null && !request.itadGameId().isBlank());
        if (fixGame) {
            GameMatchOverride current = matchOverrideService.findGameOverride(report.getGameTitle())
                    .orElse(null);
            Long igdbGameId = request.igdbGameId() != null
                    ? request.igdbGameId()
                    : (current != null ? current.getIgdbGameId() : null);
            String itadGameId = request.itadGameId() != null && !request.itadGameId().isBlank()
                    ? request.itadGameId().trim()
                    : (current != null ? current.getItadGameId() : null);
            matchOverrideService.saveGameOverride(report.getGameTitle(), igdbGameId, itadGameId, adminUserId);
        }

        boolean fixStore = request.storeName() != null && !request.storeName().isBlank()
                && (request.storeUrl() != null || Boolean.TRUE.equals(request.hideOffer()));
        if (fixStore) {
            matchOverrideService.saveStoreOverride(report.getGameTitle(), request.storeName(),
                    request.storeUrl(), Boolean.TRUE.equals(request.hideOffer()), adminUserId);
        }
    }

    private MatchReport getOrThrow(Long id) {
        return matchReportRepository.findById(id)
                .orElseThrow(() -> new MatchReportNotFoundException(id));
    }

    private MatchReportResponse toResponse(MatchReport report) {
        return new MatchReportResponse(
                report.getId(),
                report.getUser().getIdUser(),
                report.getUser().getUsername(),
                report.getType(),
                report.getStatus(),
                report.getGameTitle(),
                report.getIgdbGameId(),
                report.getItadGameId(),
                report.getStoreName(),
                report.getCurrentUrl(),
                report.getMessage(),
                report.getAdminNote(),
                report.getCreatedAt(),
                report.getResolvedAt(),
                report.getResolvedBy() != null ? report.getResolvedBy().getUsername() : null);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
