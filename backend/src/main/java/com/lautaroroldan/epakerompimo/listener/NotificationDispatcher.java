package com.lautaroroldan.epakerompimo.listener;

import com.lautaroroldan.epakerompimo.entity.Notification;
import com.lautaroroldan.epakerompimo.enums.NotificationStatus;
import com.lautaroroldan.epakerompimo.event.NotificationCreatedEvent;
import com.lautaroroldan.epakerompimo.repository.NotificationRepository;
import com.lautaroroldan.epakerompimo.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

@Component
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public NotificationDispatcher(NotificationRepository notificationRepository, EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    // Después del commit (la notificación ya existe en la base) y en otro hilo (nadie espera a Gmail)
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onNotificationCreated(NotificationCreatedEvent event) {
        Notification notification = notificationRepository.findById(event.id()).orElse(null);
        if (notification == null) {
            return;
        }

        try {
            emailService.send(notification);
        } catch (Exception e) {
            log.error("Error enviando la notificación {}", event.id(), e);
            notificationRepository.updateStatus(event.id(), NotificationStatus.FAILED, null, shorten(e.getMessage()));
            return;
        }
        notificationRepository.updateStatus(event.id(), NotificationStatus.SENT, Instant.now(), null);
    }

    private String shorten(String text) {
        if (text == null) {
            return null;
        }
        return text.length() > 500 ? text.substring(0, 500) : text;
    }
}