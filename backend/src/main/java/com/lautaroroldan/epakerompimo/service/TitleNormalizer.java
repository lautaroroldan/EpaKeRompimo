package com.lautaroroldan.epakerompimo.service;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Normalizes game titles so they can be compared across providers, which
 * disagree on casing, accents and punctuation (IGDB "Baldur's Gate 3" vs ITAD
 * "BALDUR'S GATE 3").
 */
public final class TitleNormalizer {

    private TitleNormalizer() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }

    public static boolean sameTitle(String first, String second) {
        String normalized = normalize(first);
        return !normalized.isEmpty() && normalized.equals(normalize(second));
    }
}
