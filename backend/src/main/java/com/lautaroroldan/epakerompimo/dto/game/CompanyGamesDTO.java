package com.lautaroroldan.epakerompimo.dto.game;

import java.util.List;

/**
 * One page of a company's games. {@code total} comes from IGDB's X-Count
 * header so the client can show "14 juegos" and know when to stop paging.
 */
public record CompanyGamesDTO(int total, List<CompanyGameDTO> games) {
}
