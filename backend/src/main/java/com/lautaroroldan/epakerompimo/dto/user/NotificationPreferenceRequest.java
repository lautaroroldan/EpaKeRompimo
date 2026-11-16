package com.lautaroroldan.epakerompimo.dto.user;

import jakarta.validation.constraints.NotNull;

public record NotificationPreferenceRequest(@NotNull Boolean enabled) {
}
