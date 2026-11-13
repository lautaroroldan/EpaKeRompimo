package com.lautaroroldan.epakerompimo.service;

import com.lautaroroldan.epakerompimo.dto.EmailDTO;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.scheduling.annotation.Async;

@Service
public class EmailServiceManager implements EmailService{
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    public EmailServiceManager(JavaMailSender javaMailSender, TemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }
    @Override public void sendMail(EmailDTO emailDTO){
        try {
            MimeMessage mimeMessage=javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper=new MimeMessageHelper(mimeMessage,true,"UTF-8");
            mimeMessageHelper.setTo(emailDTO.getDestinatario());
            mimeMessageHelper.setSubject(emailDTO.getAsunto());
            Context context=new Context();
            context.setVariable("message",emailDTO.getMsg());
            String msg=templateEngine.process("email",context );
            mimeMessageHelper.setText(msg,true);
            javaMailSender.send(mimeMessage);
        }catch (Exception e){
            throw new RuntimeException("Error en emailServiceManager "+ e.getMessage(), e);
        }

    }

    @Async
    @Override
    public void sendVerificationEmail(String destinatario, String username, String link) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(destinatario);
            helper.setSubject("Verificá tu mail en EpaKeRompimo");

            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("link", link);
            helper.setText(templateEngine.process("verify-email", context), true);

            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new RuntimeException("Error enviando mail de verificación: " + e.getMessage(), e);
        }
    }
}
