package com.lautaroroldan.epakerompimo.security;

import com.lautaroroldan.epakerompimo.exceptions.ForbiddenOperationException;
import org.springframework.security.core.Authentication;

public final class AuthUtils {

    private AuthUtils() {
    }

    // El JwtAuthFilter guarda el id del usuario en authentication.details
    public static Long userId(Authentication authentication) {
        if (authentication != null && authentication.getDetails() instanceof Number id) {
            return id.longValue();
        }
        throw new ForbiddenOperationException("No se pudo identificar al usuario autenticado");
    }
}
