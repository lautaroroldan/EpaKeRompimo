package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.favorite.FavoriteEnterpriseRequest;
import com.lautaroroldan.epakerompimo.dto.favorite.FavoriteEnterpriseResponse;
import com.lautaroroldan.epakerompimo.entity.FavoriteEnterprise;
import com.lautaroroldan.epakerompimo.entity.User;
import com.lautaroroldan.epakerompimo.exceptions.FavoriteAlreadyExistsException;
import com.lautaroroldan.epakerompimo.exceptions.FavoriteNotFoundException;
import com.lautaroroldan.epakerompimo.exceptions.UserNotFoundException;
import com.lautaroroldan.epakerompimo.repository.FavoriteEnterpriseRepository;
import com.lautaroroldan.epakerompimo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoriteEnterpriseServiceManager implements FavoriteEnterpriseService {

    private final FavoriteEnterpriseRepository favoriteEnterpriseRepository;
    private final UserRepository userRepository;

    public FavoriteEnterpriseServiceManager(FavoriteEnterpriseRepository favoriteEnterpriseRepository,
                                            UserRepository userRepository) {
        this.favoriteEnterpriseRepository = favoriteEnterpriseRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<FavoriteEnterpriseResponse> findAll(Long idUser) {
        return favoriteEnterpriseRepository.findByUser_IdUserOrderByIdDesc(idUser).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public FavoriteEnterpriseResponse add(Long idUser, FavoriteEnterpriseRequest request) {
        if (favoriteEnterpriseRepository.existsByUser_IdUserAndItadEnterpriseId(idUser, request.itadEnterpriseId())) {
            throw new FavoriteAlreadyExistsException("Esa empresa ya está en tus favoritos");
        }

        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new UserNotFoundException(idUser));

        FavoriteEnterprise favorite = new FavoriteEnterprise();
        favorite.setUser(user);
        favorite.setItadEnterpriseId(request.itadEnterpriseId());
        favorite.setName(request.name().trim());

        return toResponse(favoriteEnterpriseRepository.save(favorite));
    }

    @Override
    @Transactional
    public void remove(Long idUser, Long itadEnterpriseId) {
        long deleted = favoriteEnterpriseRepository.deleteByUser_IdUserAndItadEnterpriseId(idUser, itadEnterpriseId);
        if (deleted == 0) {
            throw new FavoriteNotFoundException("Esa empresa no está en tus favoritos");
        }
    }

    private FavoriteEnterpriseResponse toResponse(FavoriteEnterprise favorite) {
        return new FavoriteEnterpriseResponse(favorite.getId(), favorite.getItadEnterpriseId(), favorite.getName());
    }
}
