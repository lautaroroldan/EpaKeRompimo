package com.lautaroroldan.epakerompimo.dto.rawg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RAWGPagination<T>(
        Integer count,
        String next,
        String previous,
        List<T> results
) {
}
