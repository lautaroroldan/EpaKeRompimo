package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.EmailDTO;

public interface EmailService {

    public void sendMail(EmailDTO emailDTO);
}
