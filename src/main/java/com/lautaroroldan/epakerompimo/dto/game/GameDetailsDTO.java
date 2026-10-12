package com.lautaroroldan.epakerompimo.dto.game;

import java.util.List;

public record GameDetailsDTO(int rawgId, String slug, String title, String description,
                             String released, String coverImageUrl, List<String> platforms) { }
