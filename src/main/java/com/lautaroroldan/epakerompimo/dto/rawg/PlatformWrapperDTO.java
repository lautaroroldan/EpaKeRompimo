package com.lautaroroldan.epakerompimo.dto.rawg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlatformWrapperDTO(
        PlatformDTO platform,
        String released_at,
        RequirementsDTO requirements
) {
}
