package com.lautaroroldan.epakerompimo.controller;

import com.lautaroroldan.epakerompimo.dto.report.MatchReportRequest;
import com.lautaroroldan.epakerompimo.dto.report.MatchReportResponse;
import com.lautaroroldan.epakerompimo.security.AuthUtils;
import com.lautaroroldan.epakerompimo.service.MatchReportService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Reportes de matcheo de los usuarios comunes. Requieren sesión: el usuario
 * sale del token, nunca del body.
 */
@RestController
@RequestMapping("/api/reports")
public class MatchReportController {

    private final MatchReportService matchReportService;

    public MatchReportController(MatchReportService matchReportService) {
        this.matchReportService = matchReportService;
    }

    @PostMapping
    public ResponseEntity<MatchReportResponse> create(@Valid @RequestBody MatchReportRequest request,
                                                      Authentication authentication) {
        MatchReportResponse created = matchReportService.create(AuthUtils.userId(authentication), request);
        return ResponseEntity
                .created(URI.create("/api/reports/" + created.id()))
                .body(created);
    }

    @GetMapping("/mine")
    public List<MatchReportResponse> findMine(Authentication authentication) {
        return matchReportService.findMine(AuthUtils.userId(authentication));
    }
}
