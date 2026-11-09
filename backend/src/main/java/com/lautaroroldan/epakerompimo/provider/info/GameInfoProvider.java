package com.lautaroroldan.epakerompimo.provider.info;

import com.lautaroroldan.epakerompimo.dto.GameDTO;
import com.lautaroroldan.epakerompimo.dto.game.GameDetailDTO;

import java.util.List;

public interface GameInfoProvider {
    public List<GameDetailDTO> searchGame(String title);
    public GameDetailDTO findGameById(String id);
}
