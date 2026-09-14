package com.lautaroroldan.epakerompimo.user.service;

import com.lautaroroldan.epakerompimo.user.entity.User;

import java.util.List;

public interface UserService {
    List<User> findAll();
    User findById(Long id);
    User save(User user);
    User update(Long id, User user);
    boolean existsByUsername(String username);
    void deleteById(Long id);
}
