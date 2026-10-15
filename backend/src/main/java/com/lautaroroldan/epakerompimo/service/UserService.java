package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.entity.User;

import java.util.List;

public interface UserService {
    List<User> findAll();
    User findById(Long id);
    User save(User user);
    User update(Long id, User user);
    boolean existsByUsername(String username);
    void deleteById(Long id);
}