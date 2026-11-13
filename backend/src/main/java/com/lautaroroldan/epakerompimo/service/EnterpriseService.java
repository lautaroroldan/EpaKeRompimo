package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.enterprise.EnterpriseResponse;

import java.util.List;

public interface EnterpriseService {
    // Busca desarrolladoras por nombre (ej: "rockstar")
    List<EnterpriseResponse> search(String name);
}
