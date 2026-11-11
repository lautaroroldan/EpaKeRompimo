package com.lautaroroldan.epakerompimo.dto.favorite;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// El nombre es obligatorio: ITAD no tiene un endpoint para consultar una empresa por su id,
// así que se guarda al marcarla.
public record FavoriteEnterpriseRequest(
        @NotNull @Positive Long itadEnterpriseId,
        @NotBlank @Size(max = 255) String name
) {}
