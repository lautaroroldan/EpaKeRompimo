package com.lautaroroldan.epakerompimo.provider.info;

import com.lautaroroldan.epakerompimo.dto.game.CompanyDetailDTO;
import com.lautaroroldan.epakerompimo.dto.game.CompanyGamesDTO;
import com.lautaroroldan.epakerompimo.dto.game.GameDetailDTO;

import java.util.List;

public interface GameInfoProvider {
    public List<GameDetailDTO> searchGame(String title);
    public GameDetailDTO findGameById(String id);
    public CompanyDetailDTO findCompanyById(long companyId);
    public CompanyGamesDTO findGamesByCompany(long companyId, int offset, int limit);
}
