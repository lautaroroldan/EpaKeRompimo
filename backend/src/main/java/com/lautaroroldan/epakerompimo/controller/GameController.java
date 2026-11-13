package com.lautaroroldan.epakerompimo.controller;

import com.lautaroroldan.epakerompimo.dto.GameWithPricesDTO;
import com.lautaroroldan.epakerompimo.dto.game.ItadGameIdDTO;
import com.lautaroroldan.epakerompimo.service.GameSearchService;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

    private final GameSearchService gameSearchService;

    public GameController(GameSearchService gameSearchService) {
        this.gameSearchService = gameSearchService;
    }

    @GetMapping("")
    public List<GameWithPricesDTO> gamePrices(@RequestParam @NotBlank String country,
                                              @RequestParam @NotBlank String title) {
        return gameSearchService.findPriceByGameTitle(title, country);
    }

    // Id de ITAD del juego de un título (lo necesita la estrella de favoritos del perfil del juego)
    @GetMapping("/itad-id")
    public ResponseEntity<ItadGameIdDTO> itadId(@RequestParam @NotBlank String title) {
        String itadGameId = gameSearchService.findItadGameIdByTitle(title);
        return itadGameId != null ? ResponseEntity.ok(new ItadGameIdDTO(itadGameId)) : ResponseEntity.notFound().build();
    }

}
