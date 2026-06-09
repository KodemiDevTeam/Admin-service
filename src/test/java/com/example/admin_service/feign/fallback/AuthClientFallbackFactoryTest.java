package com.example.admin_service.feign.fallback;

import com.example.admin_service.dto.response.AdminLoginRequest;
import com.example.admin_service.exceptions.DownstreamServiceException;
import com.example.admin_service.feign.AuthClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthClientFallbackFactoryTest {

    private AuthClientFallbackFactory fallbackFactory;
    private AuthClient fallbackClient;

    @BeforeEach
    void setUp() {
        fallbackFactory = new AuthClientFallbackFactory();
        fallbackClient = fallbackFactory.create(new RuntimeException("Service unavailable"));
    }

    @Test
    void testAdminLoginThrowsDownstreamServiceException() {
        AdminLoginRequest request = new AdminLoginRequest();
        assertThrows(DownstreamServiceException.class, () -> {
            fallbackClient.adminLogin(request);
        });
    }

    @Test
    void testReviewTrainerThrowsDownstreamServiceException() {
        assertThrows(DownstreamServiceException.class, () -> {
            fallbackClient.reviewTrainer("token", "trainerId", null);
        });
    }

    @Test
    void testGeUserByIdThrowsDownstreamServiceException() {
        assertThrows(DownstreamServiceException.class, () -> {
            fallbackClient.geUserById("token", "userId");
        });
    }

    @Test
    void testFallbackClientImplementsAuthClient() {
        assertTrue(fallbackClient instanceof AuthClient);
    }

    @Test
    void testFallbackFactoryCreateWithDifferentCauses() {
        AuthClient client1 = fallbackFactory.create(new RuntimeException("Error 1"));
        AuthClient client2 = fallbackFactory.create(new IllegalStateException("Error 2"));

        assertNotNull(client1);
        assertNotNull(client2);
    }

    @Test
    void testMultipleFallbackCreations() {
        AuthClient fallback1 = fallbackFactory.create(new RuntimeException("Error 1"));
        AuthClient fallback2 = fallbackFactory.create(new RuntimeException("Error 2"));

        assertNotNull(fallback1);
        assertNotNull(fallback2);
        assertNotSame(fallback1, fallback2);
    }
}
