package com.lautaroroldan.epakerompimo.dto.report;

import com.lautaroroldan.epakerompimo.enums.MatchReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MatchReportRequest(
        @NotNull MatchReportType type,
        @NotBlank @Size(max = 255) String gameTitle,
        Long igdbGameId,
        @Size(max = 64) String itadGameId,
        @Size(max = 100) String storeName,
        @Size(max = 2048) String currentUrl,
        @Size(max = 1000) String message
) {}
