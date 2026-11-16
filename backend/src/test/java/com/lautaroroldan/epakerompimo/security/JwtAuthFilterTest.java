package com.lautaroroldan.epakerompimo.security;

import com.lautaroroldan.epakerompimo.dto.user.UserResponse;
import com.lautaroroldan.epakerompimo.enums.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthFilterTest {

    // Mismo secreto de desarrollo que application.properties (Base64, >= 32 bytes)
    private static final String SECRET =
            "KOksQyTkas8haWEfdieLj9vM7XG30t1cijBPNHr4rYmhBPSW+2VocnzdiR6ry/LkQevjOPDNXr5YSLCHnYrtnA==";

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void translatesTheRoleClaimIntoAnAuthority() throws Exception {
        JwtService jwtService = jwtService();
        String token = jwtService.generate(new UserResponse(7L, "admin", null, null, null, Role.ADMIN));

        authenticate(jwtService, token);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
        assertThat(authentication.getDetails()).isEqualTo(7L);
    }

    @Test
    void regularUsersOnlyGetRoleUser() throws Exception {
        JwtService jwtService = jwtService();
        String token = jwtService.generate(new UserResponse(8L, "pepe", null, null, null, Role.USER));

        authenticate(jwtService, token);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    void ignoresAnInvalidToken() throws Exception {
        JwtAuthFilter filter = new JwtAuthFilter(jwtService());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer no-es-un-token");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private static JwtService jwtService() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        return jwtService;
    }

    private static void authenticate(JwtService jwtService, String token) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        new JwtAuthFilter(jwtService).doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
    }
}
