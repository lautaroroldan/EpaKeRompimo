package com.lautaroroldan.epakerompimo.controller;

import com.lautaroroldan.epakerompimo.dto.PriceDTO;
import com.lautaroroldan.epakerompimo.service.GameSearchService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/prices")
public class PriceController {

    private final GameSearchService gameSearchService;
    private final String defaultCountry;

    public PriceController(GameSearchService gameSearchService,
                           @Value("${itad.country:US}") String defaultCountry) {
        this.gameSearchService = gameSearchService;
        this.defaultCountry = defaultCountry;
    }

    /**
     * Every store's offer for a game, cheapest first. An empty list means no
     * store has a price for it.
     */
    @GetMapping("")
    public List<PriceDTO> offers(@RequestParam @NotBlank String title,
                                 @RequestParam(required = false) String country) {
        String targetCountry = country == null || country.isBlank() ? defaultCountry : country;
        return gameSearchService.findOffersByTitle(title, targetCountry);
    }
}
