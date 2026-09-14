package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.entity.User;
import com.lautaroroldan.epakerompimo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceManager implements UserService{
    @Autowired
    private UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceManager(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<User> findAll() {
        return (List<User>) this.userRepository.findAll();
    }

    @Override
    public User findById(Long id) {
        return this.userRepository.findById(id).
                orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Override
    public User save(User user) {
        if (this.userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("User already exists with username: " + user.getUsername());
        }

        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        return this.userRepository.save(user);
    }

    @Override
    public User update(Long id, User user) {
        User existingUser = this.userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        existingUser.setUsername(user.getUsername());
        existingUser.setPassword(user.getPassword());
        existingUser.setIcon(user.getIcon());

        return this.userRepository.save(existingUser);
    }

    @Override
    public boolean existsByUsername(String username) {
        return this.userRepository.existsByUsername(username);
    }

    @Override
    public void deleteById(Long id) {
        this.userRepository.deleteById(id);
    }
}
