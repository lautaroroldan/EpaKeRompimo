package com.lautaroroldan.epakerompimo.user.repository;

import com.lautaroroldan.epakerompimo.user.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    boolean existsByUsername(String username);
}
