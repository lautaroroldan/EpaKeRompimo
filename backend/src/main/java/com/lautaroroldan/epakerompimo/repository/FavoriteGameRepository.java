package com.lautaroroldan.epakerompimo.repository;

import com.lautaroroldan.epakerompimo.entity.FavoriteGame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteGameRepository extends JpaRepository<FavoriteGame, Long> {

    List<FavoriteGame> findByUser_IdUserOrderByIdDesc(Long idUser);

    boolean existsByUser_IdUserAndItadGameId(Long idUser, String itadGameId);

    // Devuelve cuántos borró: 0 significa que ese favorito no existía
    long deleteByUser_IdUserAndItadGameId(Long idUser, String itadGameId);

    // Se usa al eliminar una cuenta
    void deleteByUser_IdUser(Long idUser);
}
