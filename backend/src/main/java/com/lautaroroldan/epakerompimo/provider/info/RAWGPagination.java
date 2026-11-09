package com.lautaroroldan.epakerompimo.provider.info;

import java.util.List;

public record RAWGPagination<T>(
        int count,
        String next,
        String previous,
        List<T> results
) {
}
