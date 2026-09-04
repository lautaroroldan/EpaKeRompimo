package com.lautaroroldan.EpaKeRompimo.controller;

import com.lautaroroldan.EpaKeRompimo.cheapshark.CheapSharkStoreDTO;
import com.lautaroroldan.EpaKeRompimo.service.CheapSharkStoreService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private final CheapSharkStoreService cheapSharkStoresService;

    public StoreController(CheapSharkStoreService cheapSharkStoresService) {
        this.cheapSharkStoresService = cheapSharkStoresService;
    }

    @GetMapping("/")
    public List<CheapSharkStoreDTO> getAllStores(){
        return cheapSharkStoresService.getAllStores();
    }
}
