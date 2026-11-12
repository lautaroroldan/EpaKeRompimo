package com.lautaroroldan.epakerompimo.controller;

import com.lautaroroldan.epakerompimo.dto.EmailDTO;
import com.lautaroroldan.epakerompimo.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class EmailController {
    @Autowired
    EmailService emailService;
    @PostMapping("/send-mail")
    private ResponseEntity<String>sendEmail(@RequestBody EmailDTO emailDTO){
        emailService.sendMail(emailDTO);
        return new ResponseEntity<>("correo enviado", HttpStatus.OK);
    }

}
