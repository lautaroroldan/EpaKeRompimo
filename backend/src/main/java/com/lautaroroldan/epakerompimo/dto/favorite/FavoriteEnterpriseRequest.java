package com.lautaroroldan.epakerompimo.dto.favorite;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// El id es el de IGDB. El nombre se usa solo si IGDB no responde al guardar
// (si responde, se guarda el nombre oficial de IGDB).
public record FavoriteEnterpriseRequest(
        @NotNull @Positive Long igdbCompanyId,
        @NotBlank @Size(max = 255) String name
) {}
