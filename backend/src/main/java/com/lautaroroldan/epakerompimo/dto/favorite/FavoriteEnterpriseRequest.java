package com.lautaroroldan.epakerompimo.dto.favorite;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FavoriteEnterpriseRequest(
        @NotNull @Positive Long rawgEnterpriseId
) {}
