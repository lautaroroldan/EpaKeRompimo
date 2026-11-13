package com.lautaroroldan.epakerompimo.controller;

import com.lautaroroldan.epakerompimo.dto.GameWithPricesDTO;
import com.lautaroroldan.epakerompimo.service.GameSearchService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/deals")
public class DealController {


    private final GameSearchService gameSearchService;

    public DealController(GameSearchService gameSearchService) {
        this.gameSearchService = gameSearchService;
    }

    @GetMapping("")
    public List<GameWithPricesDTO> findDeals(@RequestParam @NotBlank String country,
                                             @RequestParam Integer offset,
                                             @RequestParam Integer limit,
                                             @RequestParam(required = false) String sort) {
        return gameSearchService.findDeals(country, offset, limit, sort);
    }
}
