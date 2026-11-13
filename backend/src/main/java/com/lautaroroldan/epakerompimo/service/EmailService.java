package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.EmailDTO;

public interface EmailService {

    public void sendMail(EmailDTO emailDTO);
    public void sendVerificationEmail(String destinatario, String username, String link);
}
