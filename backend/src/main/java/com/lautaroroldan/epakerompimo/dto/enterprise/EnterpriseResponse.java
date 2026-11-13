package com.lautaroroldan.epakerompimo.dto.enterprise;

// Una empresa para mostrar en el buscador (el id es el de IGDB).
// logoUrl es null si IGDB no tiene logo.
public record EnterpriseResponse(Long igdbCompanyId, String name, String logoUrl) {
}
