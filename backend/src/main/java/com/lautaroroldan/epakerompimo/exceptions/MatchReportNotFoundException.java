package com.lautaroroldan.epakerompimo.exceptions;

public class MatchReportNotFoundException extends RuntimeException {
    public MatchReportNotFoundException(Long id) {
        super("No existe el reporte " + id);
    }
}
