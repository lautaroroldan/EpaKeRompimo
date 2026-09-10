package com.lautaroroldan.epakerompimo.controller;

import com.lautaroroldan.epakerompimo.client.cheapshark.CheapSharkStoreClient;
import com.lautaroroldan.epakerompimo.dto.cheapshark.CheapSharkStoreDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private final CheapSharkStoreClient cheapSharkStoreClient;

    public StoreController(CheapSharkStoreClient cheapSharkStoreClient) {
        this.cheapSharkStoreClient = cheapSharkStoreClient;
    }

    @GetMapping("/")
    public List<CheapSharkStoreDTO> getAllStores() {
        return cheapSharkStoreClient.getAllStores();
    }
}
