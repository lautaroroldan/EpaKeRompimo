package com.lautaroroldan.epakerompimo.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TitleNormalizerTest {

    @Test
    void ignoresCaseAccentsAndPunctuation() {
        assertThat(TitleNormalizer.normalize("Baldur's Gate 3")).isEqualTo("baldur s gate 3");
        assertThat(TitleNormalizer.normalize("BÁLDUR'S GATE 3")).isEqualTo("baldur s gate 3");
        assertThat(TitleNormalizer.sameTitle("Pokémon Rojo", "pokemon rojo")).isTrue();
    }

    @Test
    void doesNotTreatEmptyTitlesAsEqual() {
        assertThat(TitleNormalizer.sameTitle("", "   ")).isFalse();
        assertThat(TitleNormalizer.sameTitle(null, "")).isFalse();
    }
}
