package com.lautaroroldan.epakerompimo.controller;

import com.lautaroroldan.epakerompimo.dto.GameWithPricesDTO;
import com.lautaroroldan.epakerompimo.service.GameSearchService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/deals")
public class DealController {

    @Autowired
    private GameSearchService gameSearchService;

    @GetMapping("/")
    public List<GameWithPricesDTO> findDeals(@RequestParam @NotBlank String country,
                                             @RequestParam Integer offset,
                                             @RequestParam Integer limit) {
        return gameSearchService.findDeals(country, offset, limit);
    }
}
