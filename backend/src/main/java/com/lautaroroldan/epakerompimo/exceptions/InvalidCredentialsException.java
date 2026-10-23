package com.lautaroroldan.epakerompimo.exceptions;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Usuario o contraseña incorrectos");
    }
}
