package com.lautaroroldan.epakerompimo.provider.info;

import com.lautaroroldan.epakerompimo.dto.GameDTO;

import java.util.List;

public interface GameInfoProvider {
    public List<GameDTO> searchGame(String title);
    public GameDTO findGameById(String id);
}
