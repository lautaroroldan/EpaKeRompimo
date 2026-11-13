package com.lautaroroldan.epakerompimo.controller;

import com.lautaroroldan.epakerompimo.dto.game.CompanyDetailDTO;
import com.lautaroroldan.epakerompimo.dto.game.CompanyGamesDTO;
import com.lautaroroldan.epakerompimo.service.CompanyService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private static final int DEFAULT_PAGE_SIZE = 24;
    private static final int MAX_PAGE_SIZE = 60;

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyDetailDTO> company(@PathVariable long id) {
        CompanyDetailDTO company = companyService.findById(id);
        return company != null ? ResponseEntity.ok(company) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/games")
    public CompanyGamesDTO games(@PathVariable long id,
                                 @RequestParam(defaultValue = "0") int offset,
                                 @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int limit) {
        int safeOffset = Math.max(offset, 0);
        int safeLimit = Math.min(Math.max(limit, 1), MAX_PAGE_SIZE);
        return companyService.findGames(id, safeOffset, safeLimit);
    }
}
