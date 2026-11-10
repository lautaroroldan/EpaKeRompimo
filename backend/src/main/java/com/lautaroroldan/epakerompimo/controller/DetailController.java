package com.lautaroroldan.epakerompimo.controller;

import com.lautaroroldan.epakerompimo.dto.game.GameDetailDTO;
import com.lautaroroldan.epakerompimo.service.GameSearchService;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/details")
public class DetailController {

    private final GameSearchService gameSearchService;

    public DetailController(GameSearchService gameSearchService) {
        this.gameSearchService = gameSearchService;
    }

    @GetMapping("")
    public GameDetailDTO gameDetails(@RequestParam @NotBlank String title) {
        return gameSearchService.findGameDetailByTitle(title);
    }
}
