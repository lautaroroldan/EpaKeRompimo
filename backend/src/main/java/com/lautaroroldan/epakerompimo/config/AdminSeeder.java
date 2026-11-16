package com.lautaroroldan.epakerompimo.config;

import com.lautaroroldan.epakerompimo.entity.User;
import com.lautaroroldan.epakerompimo.enums.Role;
import com.lautaroroldan.epakerompimo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea la cuenta administradora al arrancar, si todavía no existe.
 *
 * <p>Las credenciales salen de {@code app.admin.username} / {@code app.admin.password}
 * (por defecto {@code admin} / {@code admin1234}), para poder cambiarlas con
 * {@code ADMIN_USERNAME} / {@code ADMIN_PASSWORD} sin recompilar.</p>
 *
 * <p>Si el nombre ya está tomado por una cuenta común no se la promueve: ascender
 * cuentas existentes sería una escalada de privilegios silenciosa.</p>
 */
@Slf4j
@Component
public class AdminSeeder implements ApplicationRunner {

    private static final String DEFAULT_PASSWORD = "admin1234";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public AdminSeeder(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${app.admin.username:admin}") String adminUsername,
                       @Value("${app.admin.password:" + DEFAULT_PASSWORD + "}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername != null ? adminUsername.trim() : "";
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminUsername.isBlank()) {
            log.warn("app.admin.username está vacío: no se crea la cuenta administradora");
            return;
        }

        userRepository.findByUsername(adminUsername).ifPresentOrElse(
                existing -> {
                    if (existing.isAdmin()) {
                        log.info("Cuenta administradora '{}' lista", adminUsername);
                    } else {
                        log.warn("El nombre '{}' ya está usado por una cuenta común; " +
                                        "no se la promueve a administrador",
                                adminUsername);
                    }
                },
                () -> {
                    User admin = new User();
                    admin.setUsername(adminUsername);
                    admin.setPassword(passwordEncoder.encode(adminPassword));
                    admin.setRole(Role.ADMIN);
                    userRepository.save(admin);
                    log.info("Cuenta administradora '{}' creada", adminUsername);
                    if (DEFAULT_PASSWORD.equals(adminPassword)) {
                        log.warn("La cuenta administradora usa la contraseña por defecto. " +
                                "Cambiala con ADMIN_PASSWORD antes de publicar la aplicación.");
                    }
                });
    }
}
