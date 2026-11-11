package com.lautaroroldan.epakerompimo.provider.info;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

// De la respuesta de GET /games/info/v2 solo se usan las desarrolladoras.
// (También trae "publishers" con el mismo formato, por si más adelante se quieren mostrar.)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ITADGameCompaniesResponse(List<ITADCompany> developers) {
}
