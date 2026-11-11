package com.lautaroroldan.epakerompimo.dto.favorite;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FavoriteGameRequest(
        @NotBlank @Size(max = 64) String itadGameId
) {}
