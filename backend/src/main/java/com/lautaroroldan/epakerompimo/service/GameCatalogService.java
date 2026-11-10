package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.GameDTO;
import com.lautaroroldan.epakerompimo.provider.gamecatalog.GameCatalogProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameCatalogService {
    private final GameCatalogProvider gameCatalogProvider;

    public GameCatalogService(GameCatalogProvider gameCatalogProvider) {
        this.gameCatalogProvider = gameCatalogProvider;
    }

    public List<GameDTO> searchByTitle(String title) {
        return gameCatalogProvider.searchGame(title);
    }
}
