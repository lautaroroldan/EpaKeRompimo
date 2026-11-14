package com.lautaroroldan.epakerompimo.dto.email;

import java.util.List;

public record EmailContentDTO(
        String heading,
        List<String> paragraphs,
        String buttonLabel,
        String buttonUrl,
        String footer
) {
    public EmailContentDTO(String heading, List<String> paragraphs, String footer) {
        this(heading, paragraphs, null, null, footer);//revisar
    }
}
