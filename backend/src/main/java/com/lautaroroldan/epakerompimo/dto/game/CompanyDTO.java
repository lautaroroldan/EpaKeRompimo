package com.lautaroroldan.epakerompimo.dto.game;

/**
 * A company credited on a game. ITAD has no company ids, so the id is IGDB's
 * company id and the only stable key to later fetch that company's games.
 */
public record CompanyDTO(long id, String name, boolean developer, boolean publisher) {
}
