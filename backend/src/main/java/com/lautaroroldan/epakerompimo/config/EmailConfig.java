package com.lautaroroldan.epakerompimo.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@PropertySource("classpath:application.properties")
public class EmailConfig {
    @Value("${mail.username}") private String email;

    @Value("${mail.password}") private String password;

    private Properties getMailProperties(){
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return props;
    }
    @Bean public JavaMailSender javaMailSender(){
        JavaMailSenderImpl mailSender=new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");   // <- sin esto va a localhost
        mailSender.setPort(587);
        mailSender.setJavaMailProperties(getMailProperties());
        mailSender.setUsername(email);
        mailSender.setPassword(password);
        return mailSender;
    }
    @Bean public ResourceLoader resourceLoader(){
        return new DefaultResourceLoader();
    }
}
