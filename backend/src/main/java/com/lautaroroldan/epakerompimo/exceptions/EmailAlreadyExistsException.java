package com.lautaroroldan.epakerompimo.exceptions;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("El mail ya está vinculado a otra cuenta: " + email);
    }
}
