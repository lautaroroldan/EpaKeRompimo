package com.lautaroroldan.epakerompimo.provider.gamecatalog;

import com.lautaroroldan.epakerompimo.dto.GameDTO;

import java.util.List;

public interface GameCatalogProvider {
    public List<GameDTO> searchGame(String title);
    public GameDTO findGameById(String id);
}
