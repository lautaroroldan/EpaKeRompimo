package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.entity.Notification;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

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
    public void send(Notification notification) {
        try {
            Context context = new Context();
            context.setVariable("heading", notification.getHeading());
            context.setVariable("paragraphs", notification.getParagraphs());
            context.setVariable("buttonLabel", notification.getButtonLabel());
            context.setVariable("buttonUrl", notification.getButtonUrl());
            context.setVariable("footer", notification.getFooter());

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(notification.getRecipient());
            helper.setSubject(notification.getSubject());
            helper.setText(templateEngine.process("email", context), true);

            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Error enviando mail '" + notification.getSubject() + "': " + e.getMessage(), e);
        }
    }

}