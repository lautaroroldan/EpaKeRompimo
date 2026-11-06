package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.GameDTO;
import com.lautaroroldan.epakerompimo.dto.PriceDTO;
import com.lautaroroldan.epakerompimo.dto.favorite.FavoriteGameRequest;
import com.lautaroroldan.epakerompimo.dto.favorite.FavoriteGameResponse;
import com.lautaroroldan.epakerompimo.entity.FavoriteGame;
import com.lautaroroldan.epakerompimo.entity.User;
import com.lautaroroldan.epakerompimo.exceptions.FavoriteAlreadyExistsException;
import com.lautaroroldan.epakerompimo.exceptions.FavoriteNotFoundException;
import com.lautaroroldan.epakerompimo.exceptions.UserNotFoundException;
import com.lautaroroldan.epakerompimo.provider.info.GameInfoProvider;
import com.lautaroroldan.epakerompimo.provider.price.PriceProvider;
import com.lautaroroldan.epakerompimo.repository.FavoriteGameRepository;
import com.lautaroroldan.epakerompimo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Sin @Transactional a nivel clase a propósito: findAll llama a APIs externas
// y no conviene tener una conexión a la base abierta mientras se espera esa respuesta.
@Service
public class FavoriteGameServiceManager implements FavoriteGameService {

    private final FavoriteGameRepository favoriteGameRepository;
    private final UserRepository userRepository;
    private final List<PriceProvider> priceProviders;
    private final List<GameInfoProvider> gameInfoProviders;

    public FavoriteGameServiceManager(FavoriteGameRepository favoriteGameRepository,
                                      UserRepository userRepository,
                                      List<PriceProvider> priceProviders,
                                      List<GameInfoProvider> gameInfoProviders) {
        this.favoriteGameRepository = favoriteGameRepository;
        this.userRepository = userRepository;
        this.priceProviders = priceProviders;
        this.gameInfoProviders = gameInfoProviders;
    }

    @Override
    public List<FavoriteGameResponse> findAll(Long idUser, String country) {
        List<FavoriteGame> favorites = favoriteGameRepository.findByUser_IdUserOrderByIdDesc(idUser);
        if (favorites.isEmpty()) {
            return List.of();
        }

        String[] ids = favorites.stream().map(FavoriteGame::getItadGameId).toArray(String[]::new);

        // Precios de todos los favoritos en una sola consulta por proveedor
        Map<String, List<PriceDTO>> pricesByGameId = new HashMap<>();
        for (PriceProvider priceProvider : priceProviders) {
            priceProvider.findPriceById(ids, country).forEach((gameId, prices) ->
                    pricesByGameId.merge(gameId, new ArrayList<>(prices), (existing, nuevos) -> {
                        existing.addAll(nuevos);
                        return existing;
                    }));
        }

        return favorites.stream()
                .map(f -> new FavoriteGameResponse(
                        f.getId(),
                        f.getItadGameId(),
                        f.getRawgGameId(),
                        findGame(f.getItadGameId()),
                        pricesByGameId.getOrDefault(f.getItadGameId(), List.of())))
                .toList();
    }

    @Override
    public List<String> findItadGameIds(Long idUser) {
        return favoriteGameRepository.findByUser_IdUserOrderByIdDesc(idUser).stream()
                .map(FavoriteGame::getItadGameId)
                .toList();
    }

    @Override
    @Transactional
    public FavoriteGameResponse add(Long idUser, FavoriteGameRequest request) {
        if (favoriteGameRepository.existsByUser_IdUserAndItadGameId(idUser, request.itadGameId())) {
            throw new FavoriteAlreadyExistsException("Ese juego ya está en tus favoritos");
        }

        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new UserNotFoundException(idUser));

        FavoriteGame favorite = new FavoriteGame();
        favorite.setUser(user);
        favorite.setItadGameId(request.itadGameId());
        favorite.setRawgGameId(request.rawgGameId());

        FavoriteGame saved = favoriteGameRepository.save(favorite);
        return new FavoriteGameResponse(saved.getId(), saved.getItadGameId(), saved.getRawgGameId(), null, List.of());
    }

    @Override
    @Transactional
    public void remove(Long idUser, String itadGameId) {
        long deleted = favoriteGameRepository.deleteByUser_IdUserAndItadGameId(idUser, itadGameId);
        if (deleted == 0) {
            throw new FavoriteNotFoundException("Ese juego no está en tus favoritos");
        }
    }

    // Busca los datos del juego (título, imagen) en el primer proveedor que lo conozca
    private GameDTO findGame(String itadGameId) {
        for (GameInfoProvider provider : gameInfoProviders) {
            GameDTO game = provider.findGameById(itadGameId);
            if (game != null) {
                return game;
            }
        }
        return null;
    }
}
