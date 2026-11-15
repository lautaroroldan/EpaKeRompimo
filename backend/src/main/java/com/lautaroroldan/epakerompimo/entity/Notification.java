package com.lautaroroldan.epakerompimo.entity;

import com.lautaroroldan.epakerompimo.enums.NotificationStatus;
import com.lautaroroldan.epakerompimo.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A quién se le notificó
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Mail al que se envió. copia
    @Column(nullable = false, length = 254)
    private String recipient;

    // Por qué se disparó
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    // Contenido del mail
    @Column(nullable = false)
    private String subject;
    @Column(nullable = false)
    private String heading;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "notification_paragraphs", joinColumns = @JoinColumn(name = "notification_id"))
    @OrderColumn(name = "paragraph_order")
    @Column(name = "paragraph", length = 1000)
    private List<String> paragraphs = new ArrayList<>();
    private String buttonLabel;
    @Column(length = 2048)
    private String buttonUrl;
    @Column(length = 500)
    private String footer;

    // Estado del envío
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.PENDING;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    private Instant sentAt;
    @Column(length = 500)
    private String errorMessage;

    public Notification() {
    }

    public Notification(User user, String recipient, NotificationType type, String subject,
                        String heading, List<String> paragraphs,
                        String buttonLabel, String buttonUrl, String footer) {
        this.user = user;
        this.recipient = recipient;
        this.type = type;
        this.subject = subject;
        this.heading = heading;
        this.paragraphs = new ArrayList<>(paragraphs);
        this.buttonLabel = buttonLabel;
        this.buttonUrl = buttonUrl;
        this.footer = footer;
    }
}