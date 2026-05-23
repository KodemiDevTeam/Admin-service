package com.example.admin_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    @Async
    public void sendOEmail(String toEmail, String password, String username) {
        if (toEmail == null || toEmail.isBlank()) {
            throw new IllegalArgumentException("Recipient email must not be null or empty");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password must not be null or empty");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be null or empty");
        }

        log.info("Sending Onboarding email");
        SimpleMailMessage message = getSimpleMailMessage(toEmail, password, username);

        try {
            mailSender.send(message);
            log.info("OTP email sent successfully");
        } catch (Exception e) {
            log.error("Failed to send OTP email", e);
            throw e;
        }
    }

    private SimpleMailMessage getSimpleMailMessage(String toEmail, String password, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("KodeMI - Onboarding");
        message.setText(
                "Hello " + username + ",\n\n" +
                        "Your Sub Admin account has been created successfully.\n\n" +
                        "Login Credentials:\n" +
                        "Email: " + toEmail + "\n" +
                        "Password: " + password + "\n\n" +
                        "You can now log in to the Admin Portal.\n\n" +
                        "Thanks,\n" +
                        "Super Admin Team"
        );
        return message;
    }
}