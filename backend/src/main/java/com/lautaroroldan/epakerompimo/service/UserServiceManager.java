package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.user.LoginRequest;
import com.lautaroroldan.epakerompimo.dto.user.UserRequest;
import com.lautaroroldan.epakerompimo.dto.user.UserResponse;
import com.lautaroroldan.epakerompimo.dto.user.UserUpdateRequest;
import com.lautaroroldan.epakerompimo.entity.User;
import com.lautaroroldan.epakerompimo.exceptions.InvalidCredentialsException;
import com.lautaroroldan.epakerompimo.exceptions.UserNotFoundException;
import com.lautaroroldan.epakerompimo.exceptions.UsernameAlreadyExistsException;
import com.lautaroroldan.epakerompimo.repository.FavoriteEnterpriseRepository;
import com.lautaroroldan.epakerompimo.repository.FavoriteGameRepository;
import com.lautaroroldan.epakerompimo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lautaroroldan.epakerompimo.exceptions.EmailAlreadyExistsException;
import com.lautaroroldan.epakerompimo.exceptions.EmailVerificationException;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserServiceManager implements UserService {

    private final UserRepository userRepository;
    private final FavoriteGameRepository favoriteGameRepository;
    private final FavoriteEnterpriseRepository favoriteEnterpriseRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final String frontendUrl;

    public UserServiceManager(UserRepository userRepository,
                              FavoriteGameRepository favoriteGameRepository,
                              FavoriteEnterpriseRepository favoriteEnterpriseRepository,
                              PasswordEncoder passwordEncoder,EmailService emailService,
                              @Value("${app.frontend-url:http://localhost:4200}") String frontendUrl) {
        this.userRepository = userRepository;
        this.favoriteGameRepository = favoriteGameRepository;
        this.favoriteEnterpriseRepository = favoriteEnterpriseRepository;
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

        User saved = userRepository.save(user);
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
        boolean enviarVerificacion = applyEmail(user, request.email());

        // Si no mandan password (o viene vacía) se conserva la actual
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        if (enviarVerificacion) {                                           // NUEVO
            sendVerificationEmail(user);
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
        // Primero los favoritos: tienen una clave foránea hacia el usuario
        favoriteGameRepository.deleteByUser_IdUser(id);
        favoriteEnterpriseRepository.deleteByUser_IdUser(id);
        userRepository.deleteById(id);
    }


    private User getOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getIdUser(), user.getUsername(), user.getIcon(),
                user.getEmail(), user.getPendingEmail());
    }
    @Override
    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .filter(u -> u.getPendingEmail() != null
                        && u.getVerificationTokenExpiry() != null
                        && u.getVerificationTokenExpiry().isAfter(Instant.now()))
                .orElseThrow(() -> new EmailVerificationException(
                        "El link de confirmación no es válido o ya venció"));

        // Por si otra cuenta confirmó el mismo mail mientras tanto
        ensureEmailFree(user.getPendingEmail(), user.getIdUser());

        // Recién acá el mail se guarda como definitivo
        user.setEmail(user.getPendingEmail());
        clearPending(user);
    }

    @Override
    @Transactional
    public void resendVerification(Long id) {
        User user = getOrThrow(id);
        if (user.getPendingEmail() == null) {
            throw new EmailVerificationException("No hay ningún mail pendiente de confirmar");
        }
        issueToken(user);
        sendVerificationEmail(user);
    }

    // Devuelve true si hay que mandar un mail de confirmación.
    // El mail nuevo NO reemplaza al confirmado: queda como pendiente hasta que se confirme.
    private boolean applyEmail(User user, String rawEmail) {
        String email = (rawEmail == null || rawEmail.isBlank()) ? null : rawEmail.trim().toLowerCase();

        if (email == null) {                              // lo borró: se quita el confirmado y el pendiente
            user.setEmail(null);
            clearPending(user);
            return false;
        }
        if (email.equals(user.getEmail())) {              // volvió a su mail confirmado: cancela el pendiente
            clearPending(user);
            return false;
        }
        if (email.equals(user.getPendingEmail())) {       // ya estaba pendiente: no se reenvía solo
            return false;
        }

        ensureEmailFree(email, user.getIdUser());
        user.setPendingEmail(email);
        issueToken(user);
        return true;
    }

    // Ocupado = otra cuenta ya lo tiene confirmado
    private void ensureEmailFree(String email, Long userId) {
        userRepository.findByEmail(email)
                .filter(owner -> !owner.getIdUser().equals(userId))
                .ifPresent(owner -> {
                    throw new EmailAlreadyExistsException(email);
                });
    }

    private void issueToken(User user) {
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationTokenExpiry(Instant.now().plus(24, ChronoUnit.HOURS));
    }

    private void clearPending(User user) {
        user.setPendingEmail(null);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
    }

    private void sendVerificationEmail(User user) {
        String link = frontendUrl + "/verificar-email?token=" + user.getVerificationToken();
        emailService.sendVerificationEmail(user.getPendingEmail(), user.getUsername(), link);
    }
}
