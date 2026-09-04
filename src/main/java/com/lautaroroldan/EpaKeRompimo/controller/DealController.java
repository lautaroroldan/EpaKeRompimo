package com.lautaroroldan.EpaKeRompimo.controller;

import com.lautaroroldan.EpaKeRompimo.cheapshark.CheapSharkDealDTO;
import com.lautaroroldan.EpaKeRompimo.cheapshark.CheapSharkDealFilterDTO;
import com.lautaroroldan.EpaKeRompimo.service.CheapSharkDealService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/deals")
public class DealController {

    private final CheapSharkDealService cheapSharkDealService;

    public DealController(CheapSharkDealService cheapSharkDealService) {
        this.cheapSharkDealService = cheapSharkDealService;
    }

    @GetMapping("/")
    public List<CheapSharkDealDTO> searchDeals(@ModelAttribute CheapSharkDealFilterDTO filters){
        return cheapSharkDealService.searchDeals(filters);
    }
}
