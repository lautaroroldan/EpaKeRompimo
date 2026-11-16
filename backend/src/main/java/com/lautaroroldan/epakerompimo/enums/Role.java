package com.lautaroroldan.epakerompimo.enums;

/**
 * Rol de una cuenta. El registro público siempre crea {@link #USER}; el único
 * administrador se siembra al arrancar la aplicación (ver {@code AdminSeeder}).
 */
public enum Role {
    USER,
    ADMIN
}
