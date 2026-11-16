package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.report.MatchReportRequest;
import com.lautaroroldan.epakerompimo.dto.report.MatchReportResolveRequest;
import com.lautaroroldan.epakerompimo.dto.report.MatchReportResponse;
import com.lautaroroldan.epakerompimo.enums.MatchReportStatus;
import com.lautaroroldan.epakerompimo.enums.MatchReportType;

import java.util.List;

public interface MatchReportService {

    MatchReportResponse create(Long userId, MatchReportRequest request);

    List<MatchReportResponse> findMine(Long userId);

    List<MatchReportResponse> findAll(MatchReportStatus status, MatchReportType type);

    MatchReportResponse findById(Long id);

    MatchReportResponse resolve(Long id, MatchReportResolveRequest request, Long adminUserId);
}
