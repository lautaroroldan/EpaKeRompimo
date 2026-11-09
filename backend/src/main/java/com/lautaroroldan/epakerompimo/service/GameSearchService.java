package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.GameWithPricesDTO;
import com.lautaroroldan.epakerompimo.dto.game.GameDetailDTO;

import java.util.List;

public interface GameSearchService {
    List<GameWithPricesDTO> findDeals(String country, Integer offset, Integer limit);
    List<GameWithPricesDTO> findPriceByGameTitle(String title, String country);
    GameDetailDTO findGameDetailByTitle(String title);
}
