package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.entity.Notification;
import com.lautaroroldan.epakerompimo.enums.NotificationMessage;
import com.lautaroroldan.epakerompimo.enums.NotificationType;
import com.lautaroroldan.epakerompimo.entity.User;
import com.lautaroroldan.epakerompimo.event.NotificationCreatedEvent;
import com.lautaroroldan.epakerompimo.repository.NotificationRepository;
import org.springframework.context.ApplicationEventPublisher;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationServiceManager implements NotificationService {

    private static final String HEADING = "Hola {username}";

    // Se mandan siempre, aunque el usuario haya desactivado las notificaciones:
    private static final Set<NotificationType> ESSENTIAL_TYPES = Set.of(NotificationType.EMAIL_VERIFICATION, NotificationType.EMAIL_UNLINK);

    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher eventPublisher;

    public NotificationServiceManager(NotificationRepository notificationRepository,
                                      ApplicationEventPublisher eventPublisher) {
        this.notificationRepository = notificationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public void send(User user, String recipient, NotificationType type, String subject,
                     List<NotificationMessage> paragraphs, String buttonLabel, String buttonUrl,
                     NotificationMessage footer, Map<String, String> params)
    {
        if (!user.isNotificationsEnabled() && !ESSENTIAL_TYPES.contains(type)) {
            return;   // el usuario las desactivó: no se guarda ni se envía nada
        }
        Map<String, String> values = new HashMap<>(params);
        values.putIfAbsent("username", user.getUsername());

        Notification notification = new Notification(
                user,
                recipient,
                type,
                fill(subject, values),
                fill(HEADING, values),
                paragraphs.stream().map(message -> fill(message.getText(), values)).toList(),
                fill(buttonLabel, values),
                buttonUrl,
                footer != null ? fill(footer.getText(), values) : null);

        Notification saved = notificationRepository.save(notification);
        // El NotificationDispatcher la envía recién cuando la transacción se confirma
        eventPublisher.publishEvent(new NotificationCreatedEvent(saved.getId()));
    }

    @Override
    @Transactional
    public void send(User user, String recipient, NotificationType type, String subject,
                     List<NotificationMessage> paragraphs, Map<String, String> params) {
        send(user, recipient, type, subject, paragraphs, null, null, null, params);
    }

    // Reemplaza cada {clave} por su valor. Un marcador sin valor queda escrito tal cual.
    private String fill(String text, Map<String, String> values) {
        if (text == null) {
            return null;
        }
        String result = text;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
}