package com.lautaroroldan.epakerompimo.dto.user;

import com.lautaroroldan.epakerompimo.enums.Role;

public record UserResponse(Long id, String username, String icon, String email, String pendingEmail,
                           Role role) {
}
