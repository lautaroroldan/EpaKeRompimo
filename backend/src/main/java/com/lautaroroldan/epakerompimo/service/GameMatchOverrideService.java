package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.PriceDTO;
import com.lautaroroldan.epakerompimo.entity.GameMatchOverride;
import com.lautaroroldan.epakerompimo.entity.StoreLinkOverride;

import java.util.List;
import java.util.Optional;

/**
 * Correcciones manuales de matcheo. El matcher las consulta antes de resolver
 * automáticamente, así que un juego arreglado por el administrador se sirve
 * corregido para todos los usuarios.
 */
public interface GameMatchOverrideService {

    /** Override vigente para un título de perfil, si existe. */
    Optional<GameMatchOverride> findGameOverride(String title);

    /** Todos los links corregidos/ocultos de un título. */
    List<StoreLinkOverride> findStoreOverrides(String title);

    /**
     * Aplica los links corregidos a las ofertas y descarta las ofertas ocultas.
     * Si no hay correcciones, devuelve la misma lista.
     */
    List<PriceDTO> applyStoreOverrides(String title, List<PriceDTO> offers);

    /**
     * Crea o reemplaza el juego fijado para un título. Un id {@code null}
     * significa "no fijar ese proveedor".
     */
    GameMatchOverride saveGameOverride(String displayTitle, Long igdbGameId, String itadGameId,
                                       Long updatedByUserId);

    /** Crea o reemplaza la corrección de link de una tienda. */
    StoreLinkOverride saveStoreOverride(String displayTitle, String storeName, String url,
                                        boolean hidden, Long updatedByUserId);

    /** Deshace todas las correcciones de un título. */
    void clear(String title);
}
