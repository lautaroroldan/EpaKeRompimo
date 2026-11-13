package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.user.LoginRequest;
import com.lautaroroldan.epakerompimo.dto.user.UserRequest;
import com.lautaroroldan.epakerompimo.dto.user.UserResponse;
import com.lautaroroldan.epakerompimo.dto.user.UserUpdateRequest;
import com.lautaroroldan.epakerompimo.entity.User;
import com.lautaroroldan.epakerompimo.exceptions.InvalidCredentialsException;
import com.lautaroroldan.epakerompimo.exceptions.UserNotFoundException;
import com.lautaroroldan.epakerompimo.exceptions.UsernameAlreadyExistsException;
import com.lautaroroldan.epakerompimo.repository.UserRepository;
//import lombok.Value;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lautaroroldan.epakerompimo.exceptions.EmailAlreadyExistsException;
import com.lautaroroldan.epakerompimo.exceptions.EmailVerificationException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserServiceManager implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final String frontendUrl;

    public UserServiceManager(UserRepository userRepository, PasswordEncoder passwordEncoder,EmailService  emailService,
                              @Value("${app.frontend-url:http://localhost:4200}") String frontendUrl) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public UserResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Override
    @Transactional
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setIcon(request.icon());
        boolean enviarVerificacion = applyEmail(user, request.email());
        User saved=userRepository.save(user);
        if (enviarVerificacion) {
            sendVerificationEmail(saved);
        }

        return toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = getOrThrow(id);

        boolean cambioUsername = !user.getUsername().equals(request.username());
        if (cambioUsername && userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }

        user.setUsername(request.username());
        user.setIcon(request.icon());

        // Si no mandan password (o viene vacía) se conserva la actual
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        return toResponse(user);
    }

    @Override
    public UserResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.username())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return toResponse(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }



    private User getOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getIdUser(), user.getUsername(), user.getIcon(),
                user.getEmail(), user.isEmailVerified());
    }
    @Override
    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .filter(u -> u.getVerificationTokenExpiry() != null
                        && u.getVerificationTokenExpiry().isAfter(Instant.now()))
                .orElseThrow(() -> new EmailVerificationException(
                        "El link de verificación no es válido o ya venció"));

        // Por si otra cuenta verificó el mismo mail mientras tanto
        ensureEmailFree(user.getEmail(), user.getIdUser());

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
    }

    @Override
    @Transactional
    public void resendVerification(Long id) {
        User user = getOrThrow(id);
        if (user.getEmail() == null) {
            throw new EmailVerificationException("Todavía no cargaste ningún mail");
        }
        if (user.isEmailVerified()) {
            throw new EmailVerificationException("Tu mail ya está verificado");
        }
        issueToken(user);
        sendVerificationEmail(user);
    }

    // Aplica el mail recibido al usuario. Devuelve true si hay que mandar el mail de verificación.
    private boolean applyEmail(User user, String rawEmail) {
        String email = (rawEmail == null || rawEmail.isBlank()) ? null : rawEmail.trim().toLowerCase();
        if (Objects.equals(email, user.getEmail())) {
            return false; // sin cambios
        }

        user.setEmailVerified(false);
        if (email == null) { // el usuario borró su mail
            user.setEmail(null);
            user.setVerificationToken(null);
            user.setVerificationTokenExpiry(null);
            return false;
        }

        ensureEmailFree(email, user.getIdUser());
        user.setEmail(email);
        issueToken(user);
        return true;
    }

    // Solo cuenta como "ocupado" si otra cuenta ya lo verificó
    private void ensureEmailFree(String email, Long userId) {
        userRepository.findByEmailAndEmailVerifiedTrue(email)
                .filter(owner -> !owner.getIdUser().equals(userId))
                .ifPresent(owner -> {
                    throw new EmailAlreadyExistsException(email);
                });
    }

    private void issueToken(User user) {
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationTokenExpiry(Instant.now().plus(24, ChronoUnit.HOURS));
    }

    private void sendVerificationEmail(User user) {
        String link = frontendUrl + "/verificar-email?token=" + user.getVerificationToken();
        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), link);
    }
}
