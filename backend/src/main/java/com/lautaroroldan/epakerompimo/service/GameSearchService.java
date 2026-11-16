package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.GameDTO;
import com.lautaroroldan.epakerompimo.dto.GameWithPricesDTO;
import com.lautaroroldan.epakerompimo.dto.PriceDTO;
import com.lautaroroldan.epakerompimo.dto.game.GameDetailDTO;

import java.util.List;

public interface GameSearchService {
    List<GameWithPricesDTO> findDeals(String country, Integer offset, Integer limit, String sort);
    List<GameWithPricesDTO> findPriceByGameTitle(String title, String country);
    GameDetailDTO findGameDetailByTitle(String title);
    List<PriceDTO> findOffersByTitle(String title, String country);
    // Id de ITAD del juego que mejor coincide con el título (null si no hay)
    String findItadGameIdByTitle(String title);

    // ---------- Solo para el panel de administración ----------

    // Todos los juegos de IGDB que devuelve la búsqueda, para elegir el correcto
    List<GameDetailDTO> findGameCandidates(String title);
    // Todos los juegos de ITAD que devuelve la búsqueda
    List<GameDTO> findCatalogCandidates(String title);
    // Ofertas reales de un id de ITAD, sin aplicar correcciones
    List<PriceDTO> findOffersByItadId(String itadGameId, String country);
}
