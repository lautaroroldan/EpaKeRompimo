package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.favorite.FavoriteEnterpriseRequest;
import com.lautaroroldan.epakerompimo.dto.favorite.FavoriteEnterpriseResponse;

import java.util.List;

public interface FavoriteEnterpriseService {
    List<FavoriteEnterpriseResponse> findAll(Long idUser);

    FavoriteEnterpriseResponse add(Long idUser, FavoriteEnterpriseRequest request);

    void remove(Long idUser, Long igdbCompanyId);
}
