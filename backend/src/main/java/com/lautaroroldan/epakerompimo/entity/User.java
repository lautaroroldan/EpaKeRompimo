package com.lautaroroldan.epakerompimo.entity;

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

    public User() {
    }

    public User(Long idUser, String username, String password, String icon) {
        this.idUser = idUser;
        this.username = username;
        this.password = password;
        this.icon = icon;
    }
}