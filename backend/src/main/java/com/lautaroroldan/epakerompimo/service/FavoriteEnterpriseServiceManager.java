package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.client.IGDBCompanyClient;
import com.lautaroroldan.epakerompimo.dto.favorite.FavoriteEnterpriseRequest;
import com.lautaroroldan.epakerompimo.dto.favorite.FavoriteEnterpriseResponse;
import com.lautaroroldan.epakerompimo.entity.FavoriteEnterprise;
import com.lautaroroldan.epakerompimo.entity.User;
import com.lautaroroldan.epakerompimo.exceptions.FavoriteAlreadyExistsException;
import com.lautaroroldan.epakerompimo.exceptions.FavoriteNotFoundException;
import com.lautaroroldan.epakerompimo.exceptions.UserNotFoundException;
import com.lautaroroldan.epakerompimo.provider.info.IGDBCompanyResponse;
import com.lautaroroldan.epakerompimo.repository.FavoriteEnterpriseRepository;
import com.lautaroroldan.epakerompimo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoriteEnterpriseServiceManager implements FavoriteEnterpriseService {

    private final FavoriteEnterpriseRepository favoriteEnterpriseRepository;
    private final UserRepository userRepository;
    private final IGDBCompanyClient igdbCompanyClient;

    public FavoriteEnterpriseServiceManager(FavoriteEnterpriseRepository favoriteEnterpriseRepository,
                                            UserRepository userRepository,
                                            IGDBCompanyClient igdbCompanyClient) {
        this.favoriteEnterpriseRepository = favoriteEnterpriseRepository;
        this.userRepository = userRepository;
        this.igdbCompanyClient = igdbCompanyClient;
    }

    @Override
    public List<FavoriteEnterpriseResponse> findAll(Long idUser) {
        return favoriteEnterpriseRepository.findByUser_IdUserOrderByIdDesc(idUser).stream()
                .map(this::toResponse)
                .toList();
    }

    // Sin @Transactional a propósito: se llama a IGDB y no conviene tener una conexión a la base
    // abierta mientras se espera esa respuesta (save() ya es transaccional).
    @Override
    public FavoriteEnterpriseResponse add(Long idUser, FavoriteEnterpriseRequest request) {
        if (favoriteEnterpriseRepository.existsByUser_IdUserAndIgdbCompanyId(idUser, request.igdbCompanyId())) {
            throw new FavoriteAlreadyExistsException("Esa empresa ya está en tus favoritos");
        }

        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new UserNotFoundException(idUser));

        // Se consulta a IGDB por id (no por nombre): trae el nombre oficial y el logo.
        // Si IGDB falla, la empresa se guarda igual con el nombre recibido y sin logo.
        IGDBCompanyResponse company = igdbCompanyClient.findById(request.igdbCompanyId());

        FavoriteEnterprise favorite = new FavoriteEnterprise();
        favorite.setUser(user);
        favorite.setIgdbCompanyId(request.igdbCompanyId());
        favorite.setName(company != null && company.name() != null ? company.name() : request.name().trim());
        favorite.setLogoUrl(company != null ? igdbCompanyClient.logoUrlOf(company) : null);

        return toResponse(favoriteEnterpriseRepository.save(favorite));
    }

    @Override
    @Transactional
    public void remove(Long idUser, Long igdbCompanyId) {
        long deleted = favoriteEnterpriseRepository.deleteByUser_IdUserAndIgdbCompanyId(idUser, igdbCompanyId);
        if (deleted == 0) {
            throw new FavoriteNotFoundException("Esa empresa no está en tus favoritos");
        }
    }

    private FavoriteEnterpriseResponse toResponse(FavoriteEnterprise favorite) {
        return new FavoriteEnterpriseResponse(favorite.getId(), favorite.getIgdbCompanyId(), favorite.getName(),
                favorite.getLogoUrl());
    }
}
