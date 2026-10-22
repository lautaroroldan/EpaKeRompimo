package com.lautaroroldan.epakerompimo.dto.user;

public record LoginResponse(
        String token,
        Long idUsuario,
        String username,
        String icon
) {}
