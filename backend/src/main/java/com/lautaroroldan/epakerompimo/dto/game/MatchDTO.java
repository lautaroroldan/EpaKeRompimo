package com.lautaroroldan.epakerompimo.dto.game;

public record MatchDTO(Status status, Method method) {
    public enum Status { IDENTIFIER_MATCH, TITLE_METADATA_MATCH, NO_MATCH, AMBIGUOUS }

    public enum Method { RAWG_STEAM_APP_ID, CONTROLLED_TITLE_METADATA, NONE }
}
