package com.lautaroroldan.epakerompimo.controller;

import com.lautaroroldan.epakerompimo.client.cheapshark.CheapSharkDealClient;
import com.lautaroroldan.epakerompimo.dto.cheapshark.CheapSharkDealDTO;
import com.lautaroroldan.epakerompimo.dto.cheapshark.CheapSharkDealFilterDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/deals")
public class DealController {

    private final CheapSharkDealClient cheapSharkDealClient;

    public DealController(CheapSharkDealClient cheapSharkDealClient) {
        this.cheapSharkDealClient = cheapSharkDealClient;
    }

    @GetMapping("/")
    public List<CheapSharkDealDTO> searchDeals(@ModelAttribute CheapSharkDealFilterDTO filters) {
        return cheapSharkDealClient.searchDeals(filters);
    }
}
