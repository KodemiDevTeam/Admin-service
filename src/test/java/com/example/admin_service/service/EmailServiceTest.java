package com.example.admin_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    private JavaMailSender mailSender;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        emailService = new EmailService(mailSender);

        ReflectionTestUtils.setField(emailService, "fromEmail", "admin@test.com");
    }

    @Test
    void sendOEmail_success() {
        emailService.sendOEmail(
                "user@test.com",
                "Password123",
                "John");

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendOEmail_nullEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> emailService.sendOEmail(
                        null,
                        "Password123",
                        "John"));
    }

    @Test
    void sendOEmail_blankEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> emailService.sendOEmail(
                        " ",
                        "Password123",
                        "John"));
    }

    @Test
    void sendOEmail_nullPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> emailService.sendOEmail(
                        "user@test.com",
                        null,
                        "John"));
    }

    @Test
    void sendOEmail_blankPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> emailService.sendOEmail(
                        "user@test.com",
                        "",
                        "John"));
    }

    @Test
    void sendOEmail_nullUsername() {
        assertThrows(IllegalArgumentException.class,
                () -> emailService.sendOEmail(
                        "user@test.com",
                        "Password123",
                        null));
    }

    @Test
    void sendOEmail_blankUsername() {
        assertThrows(IllegalArgumentException.class,
                () -> emailService.sendOEmail(
                        "user@test.com",
                        "Password123",
                        ""));
    }

    @Test
    void sendOEmail_sendFails() {

        doThrow(new RuntimeException("Mail Error"))
                .when(mailSender)
                .send(any(SimpleMailMessage.class));

        assertThrows(RuntimeException.class,
                () -> emailService.sendOEmail(
                        "user@test.com",
                        "Password123",
                        "John"));
    }
}