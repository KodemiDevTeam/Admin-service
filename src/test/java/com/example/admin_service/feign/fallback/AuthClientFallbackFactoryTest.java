package com.example.admin_service.feign.fallback;

import com.example.admin_service.dto.request.TrainerReviewRequest;
import com.example.admin_service.dto.response.AdminLoginRequest;
import com.example.admin_service.exceptions.DownstreamServiceException;
import com.example.admin_service.feign.AuthClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthClientFallbackFactoryTest {

    private AuthClient authClient;

    @BeforeEach
    void setUp() {
        AuthClientFallbackFactory factory = new AuthClientFallbackFactory();
        authClient = factory.create(new RuntimeException("Auth Service Down"));
    }

    @Test
    void reviewTrainer_shouldThrowDownstreamServiceException() {

        TrainerReviewRequest request = new TrainerReviewRequest();

        DownstreamServiceException ex = assertThrows(
                DownstreamServiceException.class,
                () -> authClient.reviewTrainer(
                        "Bearer token",
                        "user1",
                        request)
        );

        assertEquals("Auth service is currently unavailable.", ex.getMessage());
    }

    @Test
    void checkStatus_shouldThrowDownstreamServiceException() {

        DownstreamServiceException ex = assertThrows(
                DownstreamServiceException.class,
                () -> authClient.checkStatus("test@gmail.com")
        );

        assertEquals("Auth service is currently unavailable.", ex.getMessage());
    }

    @Test
    void adminLogin_shouldThrowDownstreamServiceException() {

        AdminLoginRequest request = new AdminLoginRequest();

        DownstreamServiceException ex = assertThrows(
                DownstreamServiceException.class,
                () -> authClient.adminLogin(request)
        );

        assertEquals("Auth service is currently unavailable.", ex.getMessage());
    }

    @Test
    void getUserById_shouldThrowDownstreamServiceException() {

        DownstreamServiceException ex = assertThrows(
                DownstreamServiceException.class,
                () -> authClient.geUserById(
                        "Bearer token",
                        "user1")
        );

        assertEquals("Auth service is currently unavailable.", ex.getMessage());
    }
}