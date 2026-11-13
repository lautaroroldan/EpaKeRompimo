package com.lautaroroldan.epakerompimo.dto.favorite;

// logoUrl puede ser null: IGDB no tiene logo de todas las empresas
public record FavoriteEnterpriseResponse(Long id, Long igdbCompanyId, String name, String logoUrl) {}
