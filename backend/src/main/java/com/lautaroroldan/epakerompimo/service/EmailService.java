package com.lautaroroldan.epakerompimo.service;

public interface EmailService {
    void sendVerificationEmail(String destinatario, String username, String link);
}
