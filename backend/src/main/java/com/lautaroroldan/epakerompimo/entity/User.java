package com.lautaroroldan.epakerompimo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUser;
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    @Column(nullable = false, length = 60)
    private String password;
    private String icon;
    @Column(length = 254)
    private String email;
    @Column(name = "email_verified", nullable = false, columnDefinition = "boolean default false")
    private boolean emailVerified = false;
    @Column(length = 64)
    private String verificationToken;
    private Instant verificationTokenExpiry;

    public User() {
    }

    public User(Long idUser, String username, String password, String icon) {
        this.idUser = idUser;
        this.username = username;
        this.password = password;
        this.icon = icon;
    }

    public User(Long idUser, String username, String password, String icon, String email) {
        this.idUser = idUser;
        this.username = username;
        this.password = password;
        this.icon = icon;
        this.email = email;

    }
}