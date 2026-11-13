package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.client.IGDBCompanyClient;
import com.lautaroroldan.epakerompimo.dto.enterprise.EnterpriseResponse;
import org.springframework.stereotype.Service;

import java.util.List;

// Sin @Transactional: no usa la base de datos, solo llama a IGDB.
@Service
public class EnterpriseServiceManager implements EnterpriseService {

    private final IGDBCompanyClient igdbCompanyClient;

    public EnterpriseServiceManager(IGDBCompanyClient igdbCompanyClient) {
        this.igdbCompanyClient = igdbCompanyClient;
    }

    @Override
    public List<EnterpriseResponse> search(String name) {
        // Con menos de 2 letras no se consulta
        if (name == null || name.trim().length() < 2) {
            return List.of();
        }

        return igdbCompanyClient.searchDevelopers(name.trim()).stream()
                .map(company -> new EnterpriseResponse(company.id(), company.name(), igdbCompanyClient.logoUrlOf(company)))
                .toList();
    }
}
