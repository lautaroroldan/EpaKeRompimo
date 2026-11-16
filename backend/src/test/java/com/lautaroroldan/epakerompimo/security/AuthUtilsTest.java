package com.lautaroroldan.epakerompimo.security;

import com.lautaroroldan.epakerompimo.exceptions.ForbiddenOperationException;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthUtilsTest {

    @Test
    void readsTheUserIdFromTheTokenDetails() {
        var authentication = new UsernamePasswordAuthenticationToken("pepe", null, List.of());
        authentication.setDetails(5L);

        assertThat(AuthUtils.userId(authentication)).isEqualTo(5L);
    }

    @Test
    void failsWhenThereIsNoAuthenticatedUser() {
        assertThatThrownBy(() -> AuthUtils.userId(null))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void detectsTheAdminAuthority() {
        var admin = new UsernamePasswordAuthenticationToken("admin", null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        assertThat(AuthUtils.isAdmin(admin)).isTrue();
        assertThat(AuthUtils.isAdmin(new UsernamePasswordAuthenticationToken("pepe", null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))))).isFalse();
        assertThat(AuthUtils.isAdmin(null)).isFalse();
    }
}
