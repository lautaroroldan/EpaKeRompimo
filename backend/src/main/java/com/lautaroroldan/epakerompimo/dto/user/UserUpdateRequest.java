package com.lautaroroldan.epakerompimo.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @NotBlank @Size(max = 50) String username,
        @Size(min = 8, max = 72) String password,
        @Size(max = 255) String icon
) {}
