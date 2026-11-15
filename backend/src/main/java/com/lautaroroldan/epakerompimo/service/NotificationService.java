package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.enums.NotificationMessage;
import com.lautaroroldan.epakerompimo.enums.NotificationType;
import com.lautaroroldan.epakerompimo.entity.User;

import java.util.List;
import java.util.Map;

public interface NotificationService {

    // Notificación completa: con botón y pie opcionales (buttonLabel, buttonUrl y footer pueden ser null).
    // recipient: mail al que se envía.
    // params: valores para los {marcadores} de los textos (además de {username}).
    void send(User user, String recipient, NotificationType type, String subject,
              List<NotificationMessage> paragraphs, String buttonLabel, String buttonUrl,
              NotificationMessage footer, Map<String, String> params);

    // Notificación simple: solo asunto y párrafos
    void send(User user, String recipient, NotificationType type, String subject,
              List<NotificationMessage> paragraphs, Map<String, String> params);
}