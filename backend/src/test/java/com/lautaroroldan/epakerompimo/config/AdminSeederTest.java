package com.lautaroroldan.epakerompimo.config;

import com.lautaroroldan.epakerompimo.entity.User;
import com.lautaroroldan.epakerompimo.enums.Role;
import com.lautaroroldan.epakerompimo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminSeederTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void createsTheAdminWhenItDoesNotExist() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secreto")).thenReturn("hash");

        new AdminSeeder(userRepository, passwordEncoder, "admin", "secreto").run(null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("admin");
        assertThat(saved.getPassword()).isEqualTo("hash");
        assertThat(saved.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void doesNotPromoteAnExistingRegularAccount() {
        User common = new User();
        common.setUsername("admin");
        common.setRole(Role.USER);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(common));

        new AdminSeeder(userRepository, passwordEncoder, "admin", "secreto").run(null);

        verify(userRepository, never()).save(any());
        assertThat(common.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void doesNothingWhenTheUsernameIsBlank() {
        new AdminSeeder(userRepository, passwordEncoder, "  ", "secreto").run(null);

        verify(userRepository, never()).findByUsername(any());
    }
}
