package com.lautaroroldan.EpaKeRompimo.controller;

import com.lautaroroldan.EpaKeRompimo.cheapshark.CheapSharkDealDTO;
import com.lautaroroldan.EpaKeRompimo.cheapshark.CheapSharkGameDTO;
import com.lautaroroldan.EpaKeRompimo.cheapshark.CheapSharkStoreDTO;
import com.lautaroroldan.EpaKeRompimo.service.CheapSharkGameService;
import com.lautaroroldan.EpaKeRompimo.service.CheapSharkService;
import com.lautaroroldan.EpaKeRompimo.service.CheapSharkStoreService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController {
    private final CheapSharkService cheapSharkService;
    private final CheapSharkStoreService cheapSharkStoresService;
    private final CheapSharkGameService cheapSharkGameService;
    private static final Logger log =
            LoggerFactory.getLogger(GameController.class);


    public GameController(CheapSharkService cheapSharkService, CheapSharkStoreService cheapSharkStoresService, CheapSharkGameService cheapSharkGameService) {
        this.cheapSharkService = cheapSharkService;
        this.cheapSharkStoresService = cheapSharkStoresService;
        this.cheapSharkGameService = cheapSharkGameService;
    }

    @GetMapping("/search")
    public List<CheapSharkDealDTO> searchDeals(@RequestParam String title,
                                               @RequestParam(required = false) String storeIDs,
                                               @RequestParam Integer exact){
        log.info(exact.toString());
        return cheapSharkService.searchDeals(title, storeIDs,exact);
    }

    @GetMapping("/stores")
    public List<CheapSharkStoreDTO> searchStores(){
        return cheapSharkStoresService.searchStores();
    }

    @GetMapping("/games")
    public List<CheapSharkGameDTO> listOfGames(@RequestParam String title){
        return cheapSharkGameService.listOfGames(title);
    }
}
