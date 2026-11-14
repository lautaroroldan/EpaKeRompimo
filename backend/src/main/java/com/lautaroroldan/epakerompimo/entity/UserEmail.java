package com.lautaroroldan.epakerompimo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "user_emails")
@Getter
@Setter
public class UserEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mail confirmado. Solo se completa cuando el usuario confirma desde el link del mail
    @Column(unique = true, length = 254)
    private String address;

    // Mail que cargó y todavía no confirmó
    @Column(length = 254)
    private String pendingAddress;

    @Column(length = 64)
    private String verificationToken;

    private Instant verificationTokenExpiry;

    // Genera un token nuevo (vale 24 h) para confirmar el mail pendiente
    public void issueToken() {
        this.verificationToken = UUID.randomUUID().toString();
        this.verificationTokenExpiry = Instant.now().plus(24, ChronoUnit.HOURS);
    }

    // Descarta el mail pendiente y su token
    public void clearPending() {
        this.pendingAddress = null;
        this.verificationToken = null;
        this.verificationTokenExpiry = null;
    }

    // El pendiente pasa a ser el mail confirmado
    public void confirm() {
        this.address = this.pendingAddress;
        clearPending();
    }

    // ¿Hay un mail pendiente y el token todavía no venció?
    public boolean tokenIsValid() {
        return pendingAddress != null
                && verificationTokenExpiry != null
                && verificationTokenExpiry.isAfter(Instant.now());
    }
}