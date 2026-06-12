package com.example.admin_service.feign.fallback;

import com.example.admin_service.dto.request.TrainerReviewRequest;
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
    void testReviewTrainerThrowsDownstreamServiceException() {
        assertThrows(DownstreamServiceException.class, () -> {
            fallbackClient.reviewTrainer("token", "userId", null);
        });
    }

    @Test
    void testCheckStatusThrowsDownstreamServiceException() {
        assertThrows(DownstreamServiceException.class, () -> {
            fallbackClient.checkStatus("user@example.com");
        });
    }

    @Test
    void testAdminLoginThrowsDownstreamServiceException() {
        assertThrows(DownstreamServiceException.class, () -> {
            fallbackClient.adminLogin(null);
        });
    }

    @Test
    void testGetUserByIdThrowsDownstreamServiceException() {
        assertThrows(DownstreamServiceException.class, () -> {
            fallbackClient.geUserById("token", "userId");
        });
    }

    @Test
    void testFallbackClientImplementsAuthClient() {
        assertTrue(fallbackClient instanceof AuthClient);
    }

    @Test
    void testFallbackWithNullCause() {
        AuthClient client = fallbackFactory.create(null);
        assertNotNull(client);
        assertThrows(DownstreamServiceException.class, () -> client.checkStatus("user@example.com"));
    }

    @Test
    void testReviewTrainerWithNullCause() {
        AuthClient client = fallbackFactory.create(null);
        TrainerReviewRequest request = new TrainerReviewRequest();
        
        DownstreamServiceException exception = assertThrows(DownstreamServiceException.class, () -> 
            client.reviewTrainer("token", "userId", request)
        );
        
        assertNotNull(exception.getMessage());
    }

    @Test
    void testAdminLoginWithNullCause() {
        AuthClient client = fallbackFactory.create(null);
        AdminLoginRequest request = new AdminLoginRequest();
        
        DownstreamServiceException exception = assertThrows(DownstreamServiceException.class, () ->
            client.adminLogin(request)
        );
        
        assertNotNull(exception.getMessage());
    }

    @Test
    void testGetUserByIdWithNullCause() {
        AuthClient client = fallbackFactory.create(null);
        
        DownstreamServiceException exception = assertThrows(DownstreamServiceException.class, () ->
            client.geUserById("token", "userId")
        );
        
        assertNotNull(exception.getMessage());
    }

    @Test
    void testExceptionIncludesCause() {
        RuntimeException cause = new RuntimeException("Original error");
        AuthClient client = fallbackFactory.create(cause);
        
        DownstreamServiceException exception = assertThrows(DownstreamServiceException.class, () -> {
            client.checkStatus("user@example.com");
        });
        
        assertSame(cause, exception.getCause());
    }

    @Test
    void testAllMethodsThrowDownstreamServiceException() {
        TrainerReviewRequest reviewRequest = new TrainerReviewRequest();
        AdminLoginRequest loginRequest = new AdminLoginRequest();
        assertThrows(DownstreamServiceException.class, () -> fallbackClient.reviewTrainer("token", "id", reviewRequest));
        assertThrows(DownstreamServiceException.class, () -> fallbackClient.checkStatus("email@example.com"));
        assertThrows(DownstreamServiceException.class, () -> fallbackClient.adminLogin(loginRequest));
        assertThrows(DownstreamServiceException.class, () -> fallbackClient.geUserById("token", "userId"));
    }

    @Test
    void testMultipleFallbackCreations() {
        AuthClient fallback1 = fallbackFactory.create(new RuntimeException("Error 1"));
        AuthClient fallback2 = fallbackFactory.create(new RuntimeException("Error 2"));

        assertNotNull(fallback1);
        assertNotNull(fallback2);
        assertNotSame(fallback1, fallback2);
    }

    @Test
    void testFallbackFactoryCreateWithDifferentCauses() {
        AuthClient client1 = fallbackFactory.create(new RuntimeException("Error 1"));
        AuthClient client2 = fallbackFactory.create(new IllegalStateException("Error 2"));

        assertNotNull(client1);
        assertNotNull(client2);
        DownstreamServiceException exception1 = assertThrows(DownstreamServiceException.class, () -> client1.checkStatus("test@example.com"));
        assertNotNull(exception1);
        DownstreamServiceException exception2 = assertThrows(DownstreamServiceException.class, () -> client2.checkStatus("test@example.com"));
        assertNotNull(exception2);
    }

    @Test
    void testExceptionMessageContainsServiceUnavailable() {
        DownstreamServiceException exception = assertThrows(DownstreamServiceException.class, () -> {
            fallbackClient.checkStatus("user@example.com");
        });

        assertTrue(exception.getMessage().contains("unavailable"));
    }
}
