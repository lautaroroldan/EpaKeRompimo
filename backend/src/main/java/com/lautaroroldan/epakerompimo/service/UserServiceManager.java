package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.user.LoginRequest;
import com.lautaroroldan.epakerompimo.dto.user.UserRequest;
import com.lautaroroldan.epakerompimo.dto.user.UserResponse;
import com.lautaroroldan.epakerompimo.dto.user.UserUpdateRequest;
import com.lautaroroldan.epakerompimo.enums.NotificationType;
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
import com.lautaroroldan.epakerompimo.enums.NotificationMessage;
import org.springframework.beans.factory.annotation.Value;
import com.lautaroroldan.epakerompimo.entity.UserEmail;
import com.lautaroroldan.epakerompimo.repository.NotificationRepository;

import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class UserServiceManager implements UserService {

    private final UserRepository userRepository;
    private final FavoriteGameRepository favoriteGameRepository;
    private final FavoriteEnterpriseRepository favoriteEnterpriseRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final String frontendUrl;
    private final NotificationRepository notificationRepository;

    public UserServiceManager(UserRepository userRepository,
                              FavoriteGameRepository favoriteGameRepository,
                              FavoriteEnterpriseRepository favoriteEnterpriseRepository,
                              PasswordEncoder passwordEncoder,
                              NotificationRepository notificationRepository,
                              NotificationService notificationService,
                              @Value("${app.frontend-url:http://localhost:4200}") String frontendUrl) {
        this.userRepository = userRepository;
        this.favoriteGameRepository = favoriteGameRepository;
        this.favoriteEnterpriseRepository = favoriteEnterpriseRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
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
        notificationRepository.deleteByUser_IdUser(id);
        userRepository.deleteById(id);
    }


    private User getOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private UserResponse toResponse(User user) {
        UserEmail mail = user.getEmail();
        return new UserResponse(user.getIdUser(), user.getUsername(), user.getIcon(),
                mail != null ? mail.getAddress() : null,
                mail != null ? mail.getPendingAddress() : null);
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByEmail_VerificationToken(token)
                .filter(u -> u.getEmail().tokenIsValid())
                .orElseThrow(() -> new EmailVerificationException(
                        "El link de confirmación no es válido o ya venció"));

        UserEmail mail = user.getEmail();

        // Por si otra cuenta confirmó el mismo mail mientras tanto
        ensureEmailFree(mail.getPendingAddress(), user.getIdUser());

        // Recién acá el mail se guarda como definitivo
        mail.confirm();
    }

    @Override
    @Transactional
    public void resendVerification(Long id) {
        User user = getOrThrow(id);
        UserEmail mail = user.getEmail();
        if (mail == null || mail.getPendingAddress() == null) {
            throw new EmailVerificationException("No hay ningún mail pendiente de confirmar");
        }
        mail.issueToken();
        sendVerificationEmail(user);
    }

    // Devuelve true si hay que mandar un mail de confirmación.
    // El mail nuevo NO reemplaza al confirmado: queda como pendiente hasta que se confirme.
    private boolean applyEmail(User user, String rawEmail) {
        String address = (rawEmail == null || rawEmail.isBlank()) ? null : rawEmail.trim().toLowerCase();
        UserEmail mail = user.getEmail();

        if (address == null) {                                  // lo borró: se quita el confirmado y el pendiente
            user.setEmail(null);                                // orphanRemoval borra la fila
            return false;
        }
        if (mail != null && address.equals(mail.getAddress())) {          // volvió a su mail confirmado: cancela el pendiente
            mail.clearPending();
            return false;
        }
        if (mail != null && address.equals(mail.getPendingAddress())) {   // ya estaba pendiente: no se reenvía solo
            return false;
        }

        ensureEmailFree(address, user.getIdUser());
        if (mail == null) {
            mail = new UserEmail();
            user.setEmail(mail);
        }
        mail.setPendingAddress(address);
        mail.issueToken();
        return true;
    }

    // Ocupado = otra cuenta ya lo tiene confirmado
    private void ensureEmailFree(String address, Long userId) {
        userRepository.findByEmail_Address(address)
                .filter(owner -> !owner.getIdUser().equals(userId))
                .ifPresent(owner -> {
                    throw new EmailAlreadyExistsException(address);
                });
    }

    private void sendVerificationEmail(User user) {
        UserEmail mail = user.getEmail();
        String link = frontendUrl + "/verificar-email?token=" + mail.getVerificationToken();
        notificationService.send(
                user,
                mail.getPendingAddress(),
                NotificationType.EMAIL_VERIFICATION,
                "Confirmá tu mail en EpaKeRompimo",
                List.of(NotificationMessage.VERIFY_EMAIL_INSTRUCTIONS),
                "Confirmar mi mail", link,
                NotificationMessage.LINK_EXPIRES_FOOTER,
                Map.of());
    }
}