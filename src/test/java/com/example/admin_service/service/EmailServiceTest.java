package com.example.admin_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "fromEmail", "admin@example.com");
    }

    @Test
    void testSendOEmailSuccess() {
        String toEmail = "user@example.com";
        String password = "TestPassword123!";
        String username = "John Doe";

        emailService.sendOEmail(toEmail, password, username);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();
        assertEquals("admin@example.com", sentMessage.getFrom());
        assertEquals(toEmail, sentMessage.getTo()[0]);
        assertEquals("KodeMI - Onboarding", sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains(username));
        assertTrue(sentMessage.getText().contains(password));
        assertTrue(sentMessage.getText().contains(toEmail));
    }

    @Test
    void testSendOEmailWithNullEmail() {
        assertThrows(IllegalArgumentException.class, () -> {
            emailService.sendOEmail(null, "password", "username");
        });
    }

    @Test
    void testSendOEmailWithBlankEmail() {
        assertThrows(IllegalArgumentException.class, () -> {
            emailService.sendOEmail("", "password", "username");
        });
    }

    @Test
    void testSendOEmailWithNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            emailService.sendOEmail("user@example.com", null, "username");
        });
    }

    @Test
    void testSendOEmailWithBlankPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            emailService.sendOEmail("user@example.com", "", "username");
        });
    }

    @Test
    void testSendOEmailWithNullUsername() {
        assertThrows(IllegalArgumentException.class, () -> {
            emailService.sendOEmail("user@example.com", "password", null);
        });
    }

    @Test
    void testSendOEmailWithBlankUsername() {
        assertThrows(IllegalArgumentException.class, () -> {
            emailService.sendOEmail("user@example.com", "password", "");
        });
    }

    @Test
    void testSendOEmailMessageContent() {
        String toEmail = "test@example.com";
        String password = "SecurePass123!";
        String username = "TestUser";

        emailService.sendOEmail(toEmail, password, username);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        String content = message.getText();

        assertTrue(content.contains("Hello " + username));
        assertTrue(content.contains("Email: " + toEmail));
        assertTrue(content.contains("Password: " + password));
        assertTrue(content.contains("Sub Admin account has been created"));
        assertTrue(content.contains("KodeMI - Onboarding"));
    }

    @Test
    void testSendOEmailWithSpecialCharacters() {
        String toEmail = "user+tag@example.com";
        String password = "P@ssw0rd!@#$%";
        String username = "José García";

        emailService.sendOEmail(toEmail, password, username);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertTrue(message.getText().contains(username));
        assertTrue(message.getText().contains(password));
    }

    @Test
    void testSendOEmailMultipleCalls() {
        emailService.sendOEmail("user1@example.com", "pass1", "User1");
        emailService.sendOEmail("user2@example.com", "pass2", "User2");
        emailService.sendOEmail("user3@example.com", "pass3", "User3");

        verify(mailSender, times(3)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendOEmailLogsInfo() {
        String toEmail = "test@example.com";
        String password = "password";
        String username = "testuser";

        emailService.sendOEmail(toEmail, password, username);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendOEmailThrowsExceptionOnMailFailure() {
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThrows(RuntimeException.class, () -> {
            emailService.sendOEmail("user@example.com", "password", "username");
        });
    }

    @Test
    void testSendOEmailEmailIsSet() {
        String toEmail = "recipient@example.com";

        emailService.sendOEmail(toEmail, "password", "username");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertNotNull(message.getTo());
        assertEquals(1, message.getTo().length);
        assertEquals(toEmail, message.getTo()[0]);
    }

    @Test
    void testSendOEmailSubjectIsCorrect() {
        emailService.sendOEmail("user@example.com", "password", "username");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertEquals("KodeMI - Onboarding", message.getSubject());
    }

    @Test
    void testSendOEmailFromEmailIsSet() {
        emailService.sendOEmail("user@example.com", "password", "username");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertEquals("admin@example.com", message.getFrom());
    }
}
