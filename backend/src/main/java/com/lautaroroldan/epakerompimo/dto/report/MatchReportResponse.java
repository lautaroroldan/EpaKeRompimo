package com.lautaroroldan.epakerompimo.dto.report;

import com.lautaroroldan.epakerompimo.enums.MatchReportStatus;
import com.lautaroroldan.epakerompimo.enums.MatchReportType;

import java.time.Instant;

public record MatchReportResponse(
        Long id,
        Long userId,
        String username,
        MatchReportType type,
        MatchReportStatus status,
        String gameTitle,
        Long igdbGameId,
        String itadGameId,
        String storeName,
        String currentUrl,
        String message,
        String adminNote,
        Instant createdAt,
        Instant resolvedAt,
        String resolvedBy
) {}
