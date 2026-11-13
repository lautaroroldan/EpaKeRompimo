package com.lautaroroldan.epakerompimo.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailServiceManager implements EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    public EmailServiceManager(JavaMailSender javaMailSender, TemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    // En segundo plano: registrarse o guardar el perfil no espera a Gmail.
    // Si falla, el error queda en la consola y el usuario puede usar "Reenviar mail".
    @Async
    @Override
    public void sendVerificationEmail(String destinatario, String username, String link) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(destinatario);
            helper.setSubject("Confirmá tu mail en EpaKeRompimo");

            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("link", link);
            helper.setText(templateEngine.process("verify-email", context), true);

            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new RuntimeException("Error enviando mail de confirmación: " + e.getMessage(), e);
        }
    }
}