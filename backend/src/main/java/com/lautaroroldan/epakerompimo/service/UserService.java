package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.user.LoginRequest;
import com.lautaroroldan.epakerompimo.dto.user.UserRequest;
import com.lautaroroldan.epakerompimo.dto.user.UserResponse;
import com.lautaroroldan.epakerompimo.dto.user.UserUpdateRequest;

import java.util.List;

public interface UserService {
    List<UserResponse> findAll();

    UserResponse findById(Long id);

    UserResponse create(UserRequest request);

    UserResponse update(Long id, UserUpdateRequest request);

    UserResponse login(LoginRequest loginRequest);

    boolean existsByUsername(String username);

    void deleteById(Long id);
}
