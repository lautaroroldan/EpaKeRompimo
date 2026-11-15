package com.lautaroroldan.epakerompimo.enums;

import lombok.Getter;

// Catálogo de mensajes predefinidos
@Getter
public enum NotificationMessage {


    VERIFY_EMAIL_INSTRUCTIONS("Para guardar este mail en tu cuenta de EpaKeRompimo, confirmalo con este botón."),

    UNLINK_EMAIL_INSTRUCTIONS("Recibimos un pedido para desvincular este mail de tu cuenta de EpaKeRompimo. Si fuiste vos, confirmalo con este botón."),

    LINK_EXPIRES_FOOTER("El link vence en 24 horas. Si no fuiste vos, ignorá este mail."),

    FAVORITE_GAME_ADDED_MESSAGE("{game} fue agregado correctamente a tu lista de favoritos."),
    FAVORITE_ENTERPRISE_ADDED_MESSAGE("{enterprise} fue agregada correctamente a tu lista de favoritos.");

    private final String text;

    NotificationMessage(String text) {
        this.text = text;
    }
}