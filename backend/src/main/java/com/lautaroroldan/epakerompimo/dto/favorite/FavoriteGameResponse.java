package com.lautaroroldan.epakerompimo.dto.favorite;

import com.lautaroroldan.epakerompimo.dto.GameDTO;
import com.lautaroroldan.epakerompimo.dto.PriceDTO;

import java.util.List;

// game y prices se completan al listar (se consultan a ITAD en el momento, no se guardan).
// Al agregar un favorito, game viene en null y prices vacío.
public record FavoriteGameResponse(
        Long id,
        String itadGameId,
        Long rawgGameId,
        GameDTO game,
        List<PriceDTO> prices
) {}
