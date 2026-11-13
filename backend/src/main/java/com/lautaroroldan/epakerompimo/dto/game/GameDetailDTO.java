package com.lautaroroldan.epakerompimo.dto.game;

import java.util.List;

public record GameDetailDTO(long externalId, String slug, String title, String description,
                            String released, String coverImageUrl, List<String> platforms,
                            List<String> screenshots, String trailerUrl, List<String> tags,
                            List<CompanyDTO> companies) { }
