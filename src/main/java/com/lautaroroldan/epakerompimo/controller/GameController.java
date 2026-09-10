package com.lautaroroldan.epakerompimo.controller;

import com.lautaroroldan.epakerompimo.client.cheapshark.CheapSharkGameClient;
import com.lautaroroldan.epakerompimo.client.rawg.RAWGClient;
import com.lautaroroldan.epakerompimo.dto.cheapshark.CheapSharkGameDTO;
import com.lautaroroldan.epakerompimo.dto.rawg.RAWGGameDetailsDTO;
import com.lautaroroldan.epakerompimo.dto.rawg.RAWGGamesDTO;
import com.lautaroroldan.epakerompimo.dto.rawg.RAWGPagination;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/games")
public class GameController {

    private final CheapSharkGameClient cheapSharkGameClient;
    private final RAWGClient rawgClient;

    public GameController(CheapSharkGameClient cheapSharkGameClient, RAWGClient rawgClient) {
        this.cheapSharkGameClient = cheapSharkGameClient;
        this.rawgClient = rawgClient;
    }

    @GetMapping("/")
    public List<CheapSharkGameDTO> listOfGames(@RequestParam @NotBlank String title) {
        return cheapSharkGameClient.listOfGames(title);
    }

    @GetMapping("/games")
    public RAWGPagination<RAWGGamesDTO> listGames(@RequestParam @NotBlank String title) {
        return rawgClient.listOfGames(title);
    }

    @GetMapping("/dlcs")
    public RAWGPagination<RAWGGamesDTO> listDLCs(@RequestParam @NotNull Integer game_pk) {
        return rawgClient.listOfDLCs(game_pk);
    }

    @GetMapping("/{id}")
    public RAWGGameDetailsDTO gameDetails(@PathVariable @Positive Integer id) {
        return rawgClient.gameDetails(id);
    }
}
