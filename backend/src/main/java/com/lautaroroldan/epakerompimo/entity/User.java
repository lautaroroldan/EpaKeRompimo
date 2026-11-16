package com.lautaroroldan.epakerompimo.entity;

import com.lautaroroldan.epakerompimo.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


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
    // Queda nullable a propósito: las filas que ya existían cuando se agregó la
    // columna no tienen rol y se leen como USER (ver roleOrDefault()).
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Role role = Role.USER;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "email_id")
    private UserEmail email;
    @Column(name = "notifications_enabled", nullable = false, columnDefinition = "boolean default true")
    private boolean notificationsEnabled = true;

    public User() {
    }

    public User(Long idUser, String username, String password, String icon) {
        this.idUser = idUser;
        this.username = username;
        this.password = password;
        this.icon = icon;
    }

    /**
     * Nunca devuelve null: las cuentas anteriores a la columna {@code role} se
     * consideran usuarios comunes.
     */
    public Role roleOrDefault() {
        return role != null ? role : Role.USER;
    }

    public boolean isAdmin() {
        return roleOrDefault() == Role.ADMIN;
    }
}