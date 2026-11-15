package com.lautaroroldan.epakerompimo.enums;

import lombok.Getter;

// Catálogo de mensajes predefinidos
@Getter
public enum NotificationMessage {


    VERIFY_EMAIL_INSTRUCTIONS("Para guardar este mail en tu cuenta de EpaKeRompimo, confirmalo con este botón."),

    //
    LINK_EXPIRES_FOOTER("El link vence en 24 horas. Si no fuiste vos, ignorá este mail."),

    FAVORITE_GAME_ADDED_MESSAGE("{game} fue agregado correctamente a tu lista de favoritos.");

    private final String text;

    NotificationMessage(String text) {
        this.text = text;
    }
}