package com.lautaroroldan.epakerompimo.repository;

import com.lautaroroldan.epakerompimo.entity.FavoriteEnterprise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteEnterpriseRepository extends JpaRepository<FavoriteEnterprise, Long> {

    List<FavoriteEnterprise> findByUser_IdUserOrderByIdDesc(Long idUser);

    boolean existsByUser_IdUserAndItadEnterpriseId(Long idUser, Long itadEnterpriseId);

    // Devuelve cuántos borró: 0 significa que esa empresa favorita no existía
    long deleteByUser_IdUserAndItadEnterpriseId(Long idUser, Long itadEnterpriseId);

    // Se usa al eliminar una cuenta
    void deleteByUser_IdUser(Long idUser);
}
