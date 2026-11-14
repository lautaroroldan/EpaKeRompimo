package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.email.EmailContentDTO;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
public class EmailServiceManager implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceManager.class);
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    public EmailServiceManager(JavaMailSender javaMailSender, TemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public void send(String destinatario, String asunto, EmailContentDTO content) {
        log.info("Enviando mail '{}'", asunto);
        try {
            Context context = new Context();
            context.setVariable("heading", content.heading());
            context.setVariable("paragraphs", content.paragraphs());
            context.setVariable("buttonLabel", content.buttonLabel());
            context.setVariable("buttonUrl", content.buttonUrl());
            context.setVariable("footer", content.footer());

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(templateEngine.process("email", context), true);

            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new RuntimeException("Error enviando mail '" + asunto + "': " + e.getMessage(), e);
        }
    }

    // En segundo plano: registrarse o guardar el perfil no espera a Gmail.
    // Si falla, el error queda en la consola y el usuario puede usar "Reenviar mail".
    @Async
    @Override
    public void sendVerificationEmail(String destinatario, String username, String link) {
        EmailContentDTO content = new EmailContentDTO(
                "Hola " + username,
                List.of("Para guardar este mail en tu cuenta de EpaKeRompimo, confirmalo con este botón."),
                "Confirmar mi mail",
                link,
                "El link vence en 24 horas. Si no fuiste vos, ignorá este mail.");
        send(destinatario, "Confirmá tu mail en EpaKeRompimo", content);
    }
}