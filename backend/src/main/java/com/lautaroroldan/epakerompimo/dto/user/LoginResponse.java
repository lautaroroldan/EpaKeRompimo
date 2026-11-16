package com.lautaroroldan.epakerompimo.dto.user;

import com.lautaroroldan.epakerompimo.enums.Role;

public record LoginResponse(
        String token,
        Long idUsuario,
        String username,
        String icon,
        Role role
) {}
