package com.lautaroroldan.epakerompimo.controller;

import com.lautaroroldan.epakerompimo.dto.GameWithPricesDTO;
import com.lautaroroldan.epakerompimo.service.GameSearchService;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/games")
public class GameController {

    @Autowired
    private GameSearchService gameSearchService;

    @GetMapping("/")
    public List<GameWithPricesDTO> gamePrices(@RequestParam @NotBlank String country,
                                              @RequestParam @NotBlank String title) {
        return gameSearchService.findPriceByGameTitle(title, country);
    }

}
