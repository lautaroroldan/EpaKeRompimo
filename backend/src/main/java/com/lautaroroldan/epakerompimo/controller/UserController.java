package com.lautaroroldan.epakerompimo.controller;

import com.lautaroroldan.epakerompimo.dto.user.LoginRequest;
import com.lautaroroldan.epakerompimo.dto.user.LoginResponse;
import com.lautaroroldan.epakerompimo.dto.user.UserRequest;
import com.lautaroroldan.epakerompimo.dto.user.UserResponse;
import com.lautaroroldan.epakerompimo.dto.user.UserUpdateRequest;
import com.lautaroroldan.epakerompimo.exceptions.ForbiddenOperationException;
import com.lautaroroldan.epakerompimo.security.AuthUtils;
import com.lautaroroldan.epakerompimo.security.JwtService;
import com.lautaroroldan.epakerompimo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @GetMapping
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        UserResponse creado = userService.create(request);
        return ResponseEntity
                .created(URI.create("/api/users/" + creado.id()))
                .body(creado);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id,
                               @Valid @RequestBody UserUpdateRequest request,
                               Authentication authentication) {
        checkOwner(id, authentication);
        return userService.update(id, request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        UserResponse user = userService.login(loginRequest);
        return new LoginResponse(jwtService.generate(user), user.id(), user.username(), user.icon());
    }

    @GetMapping("/exists/{username}")
    public boolean existsByUsername(@PathVariable String username) {
        return userService.existsByUsername(username);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id, Authentication authentication) {
        checkOwner(id, authentication);
        userService.deleteById(id);
    }

    // Solo el dueño de la cuenta puede modificarla o borrarla
    private void checkOwner(Long id, Authentication authentication) {
        if (!id.equals(AuthUtils.userId(authentication))) {
            throw new ForbiddenOperationException("No podés modificar la cuenta de otro usuario");
        }
    }
}
