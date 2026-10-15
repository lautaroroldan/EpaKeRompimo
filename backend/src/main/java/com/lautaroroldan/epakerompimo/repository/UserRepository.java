package com.lautaroroldan.epakerompimo.repository;

import com.lautaroroldan.epakerompimo.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    boolean existsByUsername(String username);
}