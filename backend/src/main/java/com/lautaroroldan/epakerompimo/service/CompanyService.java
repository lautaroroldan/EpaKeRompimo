package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.game.CompanyDetailDTO;
import com.lautaroroldan.epakerompimo.dto.game.CompanyGamesDTO;
import com.lautaroroldan.epakerompimo.provider.info.GameInfoProvider;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {

    private final GameInfoProvider gameInfoProvider;

    public CompanyService(GameInfoProvider gameInfoProvider) {
        this.gameInfoProvider = gameInfoProvider;
    }

    public CompanyDetailDTO findById(long companyId) {
        return gameInfoProvider.findCompanyById(companyId);
    }

    public CompanyGamesDTO findGames(long companyId, int offset, int limit) {
        return gameInfoProvider.findGamesByCompany(companyId, offset, limit);
    }
}
