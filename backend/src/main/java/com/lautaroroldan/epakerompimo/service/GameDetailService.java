package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.game.GameDetailDTO;
import com.lautaroroldan.epakerompimo.provider.info.GameInfoProvider;
import org.springframework.stereotype.Service;

@Service
public class GameDetailService {
    private final GameInfoProvider gameInfoProvider;

    public GameDetailService(GameInfoProvider gameInfoProvider) {
        this.gameInfoProvider = gameInfoProvider;
    }

    public GameDetailDTO findByTitle(String title) {
        return gameInfoProvider.searchGame(title).stream().findFirst().orElse(null);
    }
}
