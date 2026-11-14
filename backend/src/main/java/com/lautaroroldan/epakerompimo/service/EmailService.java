package com.lautaroroldan.epakerompimo.service;
import com.lautaroroldan.epakerompimo.dto.email.EmailContentDTO;

public interface EmailService {
    void send(String destinatario, String asunto, EmailContentDTO content);
    void sendVerificationEmail(String destinatario, String username, String link);
}
