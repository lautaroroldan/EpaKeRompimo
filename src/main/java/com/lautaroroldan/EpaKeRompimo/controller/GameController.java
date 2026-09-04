package com.lautaroroldan.EpaKeRompimo.controller;

import com.lautaroroldan.EpaKeRompimo.cheapshark.CheapSharkGameDTO;
import com.lautaroroldan.EpaKeRompimo.cheapshark.CheapSharkStoreDTO;
import com.lautaroroldan.EpaKeRompimo.service.CheapSharkGameService;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final CheapSharkGameService cheapSharkGameService;
    private static final Logger log =
            LoggerFactory.getLogger(GameController.class);

    public GameController(CheapSharkGameService cheapSharkGameService) {
        this.cheapSharkGameService = cheapSharkGameService;
    }

    @GetMapping("/")
    public List<CheapSharkGameDTO> listOfGames(@RequestParam String title){
        return cheapSharkGameService.listOfGames(title);
    }
}
