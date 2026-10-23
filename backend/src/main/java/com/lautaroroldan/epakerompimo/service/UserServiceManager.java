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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserServiceManager implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceManager(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

        return toResponse(userRepository.save(user));
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
        return new UserResponse(user.getIdUser(), user.getUsername(), user.getIcon());
    }
}
