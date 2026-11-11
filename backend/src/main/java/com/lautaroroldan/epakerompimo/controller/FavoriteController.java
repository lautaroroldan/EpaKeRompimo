package com.lautaroroldan.epakerompimo.controller;

import com.lautaroroldan.epakerompimo.dto.enterprise.EnterpriseResponse;
import com.lautaroroldan.epakerompimo.dto.favorite.FavoriteEnterpriseRequest;
import com.lautaroroldan.epakerompimo.dto.favorite.FavoriteEnterpriseResponse;
import com.lautaroroldan.epakerompimo.dto.favorite.FavoriteGameRequest;
import com.lautaroroldan.epakerompimo.dto.favorite.FavoriteGameResponse;
import com.lautaroroldan.epakerompimo.security.AuthUtils;
import com.lautaroroldan.epakerompimo.service.FavoriteEnterpriseService;
import com.lautaroroldan.epakerompimo.service.FavoriteGameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

// Todas estas rutas requieren estar logueado (las cubre anyRequest().authenticated() de SecurityConfig).
// El usuario se toma del token, nunca de un parámetro: nadie puede tocar los favoritos de otro.
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteGameService favoriteGameService;
    private final FavoriteEnterpriseService favoriteEnterpriseService;

    public FavoriteController(FavoriteGameService favoriteGameService,
                              FavoriteEnterpriseService favoriteEnterpriseService) {
        this.favoriteGameService = favoriteGameService;
        this.favoriteEnterpriseService = favoriteEnterpriseService;
    }

    // ---------- Juegos ----------

    @GetMapping("/games")
    public List<FavoriteGameResponse> findGames(@RequestParam(defaultValue = "AR") String country,
                                                Authentication authentication) {
        return favoriteGameService.findAll(AuthUtils.userId(authentication), country);
    }

    @GetMapping("/games/ids")
    public List<String> findGameIds(Authentication authentication) {
        return favoriteGameService.findItadGameIds(AuthUtils.userId(authentication));
    }

    // Empresas desarrolladoras de un juego favorito (según ITAD), para marcarlas como favoritas
    @GetMapping("/games/{itadGameId}/enterprises")
    public List<EnterpriseResponse> findGameEnterprises(@PathVariable String itadGameId,
                                                        Authentication authentication) {
        return favoriteGameService.findEnterprises(AuthUtils.userId(authentication), itadGameId);
    }

    @PostMapping("/games")
    public ResponseEntity<FavoriteGameResponse> addGame(@Valid @RequestBody FavoriteGameRequest request,
                                                        Authentication authentication) {
        FavoriteGameResponse creado = favoriteGameService.add(AuthUtils.userId(authentication), request);
        return ResponseEntity
                .created(URI.create("/api/favorites/games/" + creado.itadGameId()))
                .body(creado);
    }

    @DeleteMapping("/games/{itadGameId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeGame(@PathVariable String itadGameId, Authentication authentication) {
        favoriteGameService.remove(AuthUtils.userId(authentication), itadGameId);
    }

    // ---------- Empresas ----------

    @GetMapping("/enterprises")
    public List<FavoriteEnterpriseResponse> findEnterprises(Authentication authentication) {
        return favoriteEnterpriseService.findAll(AuthUtils.userId(authentication));
    }

    @PostMapping("/enterprises")
    public ResponseEntity<FavoriteEnterpriseResponse> addEnterprise(@Valid @RequestBody FavoriteEnterpriseRequest request,
                                                                    Authentication authentication) {
        FavoriteEnterpriseResponse creado = favoriteEnterpriseService.add(AuthUtils.userId(authentication), request);
        return ResponseEntity
                .created(URI.create("/api/favorites/enterprises/" + creado.itadEnterpriseId()))
                .body(creado);
    }

    @DeleteMapping("/enterprises/{itadEnterpriseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeEnterprise(@PathVariable Long itadEnterpriseId, Authentication authentication) {
        favoriteEnterpriseService.remove(AuthUtils.userId(authentication), itadEnterpriseId);
    }
}
