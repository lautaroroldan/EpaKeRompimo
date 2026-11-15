package com.lautaroroldan.epakerompimo.repository;

import com.lautaroroldan.epakerompimo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail_Address(String address);

    @Query("select u from User u left join fetch u.email where u.idUser = :id")
    Optional<User> findWithEmailById(@Param("id") Long id);

    Optional<User> findByEmail_VerificationToken(String verificationToken);
}
