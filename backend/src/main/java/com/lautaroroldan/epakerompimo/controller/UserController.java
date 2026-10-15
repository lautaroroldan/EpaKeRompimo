package com.lautaroroldan.epakerompimo.controller;

import com.lautaroroldan.epakerompimo.entity.User;
import com.lautaroroldan.epakerompimo.service.UserServiceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserServiceManager userServiceManager;

    @GetMapping
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return this.userServiceManager.findAll();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public User findById(@PathVariable Long id) {
        return this.userServiceManager.findById(id);
    }

    @PostMapping()
    @Transactional
    public User save(@RequestBody User user) {
        return this.userServiceManager.save(user);
    }

    @PutMapping("/{id}")
    @Transactional
    public User update(@PathVariable Long id, @RequestBody User user) {
        return this.userServiceManager.update(id, user);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void deleteById(@PathVariable Long id) {
        this.userServiceManager.deleteById(id);
    }
}