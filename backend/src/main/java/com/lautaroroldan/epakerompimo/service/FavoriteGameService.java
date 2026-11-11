package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.enterprise.EnterpriseResponse;
import com.lautaroroldan.epakerompimo.dto.favorite.FavoriteGameRequest;
import com.lautaroroldan.epakerompimo.dto.favorite.FavoriteGameResponse;

import java.util.List;

public interface FavoriteGameService {
    // Lista con título, imagen y precios actuales (se consultan a ITAD en el momento)
    List<FavoriteGameResponse> findAll(Long idUser, String country);

    // Solo los ids de ITAD: es barato, sirve para marcar las estrellas en el buscador
    List<String> findItadGameIds(Long idUser);

    FavoriteGameResponse add(Long idUser, FavoriteGameRequest request);

    List<EnterpriseResponse> findEnterprises(Long idUser, String itadGameId);

    void remove(Long idUser, String itadGameId);
}
