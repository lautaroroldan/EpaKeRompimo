package com.lautaroroldan.epakerompimo.dto.admin;

import java.time.Instant;

public record StoreLinkOverrideResponse(
        String storeName,
        String url,
        boolean hidden,
        Instant updatedAt
) {}
