package com.lautaroroldan.epakerompimo.provider.gamecatalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Una empresa tal como la devuelve ITAD: {"id": 434, "name": "Paradox Development Studio"}
@JsonIgnoreProperties(ignoreUnknown = true)
public record ITADCompany(Long id, String name) {
}
