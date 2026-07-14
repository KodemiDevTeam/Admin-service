package com.example.admin_service.feign.fallback;

import com.example.admin_service.exceptions.DownstreamServiceException;
import com.example.admin_service.feign.UserClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserClientFallbackFactoryTest {

    private final UserClientFallbackFactory fallbackFactory =
            new UserClientFallbackFactory();

    @Test
    void testCreateReturnsFallbackInstance() {
        Throwable cause = new RuntimeException("User Service Down");

        UserClient fallback = fallbackFactory.create(cause);

        assertNotNull(fallback);
    }

    @Test
    void testGetTrainerByIdThrowsException() {
        UserClient fallback = fallbackFactory.create(new RuntimeException("User Service Down"));

        DownstreamServiceException exception = assertThrows(
                DownstreamServiceException.class,
                () -> fallback.getTrainerById("Bearer token", "trainer123")
        );

        assertEquals("User service is currently unavailable.", exception.getMessage());
    }

    @Test
    void testGetAllTrainersThrowsException() {
        UserClient fallback = fallbackFactory.create(new RuntimeException("User Service Down"));

        DownstreamServiceException exception = assertThrows(
                DownstreamServiceException.class,
                () -> fallback.getAllTrainers("Bearer token")
        );

        assertEquals("User service is currently unavailable.", exception.getMessage());
    }

    @Test
    void testGetAdminThrowsException() {
        UserClient fallback = fallbackFactory.create(new RuntimeException("User Service Down"));

        DownstreamServiceException exception = assertThrows(
                DownstreamServiceException.class,
                () -> fallback.getAdmin("admin123")
        );

        assertEquals("User service is currently unavailable.", exception.getMessage());
    }

    @Test
    void testGetAllPendingTrainersThrowsException() {
        UserClient fallback = fallbackFactory.create(new RuntimeException("User Service Down"));

        DownstreamServiceException exception = assertThrows(
                DownstreamServiceException.class,
                () -> fallback.getAllPendingTrainers("Bearer token")
        );

        assertEquals("User service is currently unavailable.", exception.getMessage());
    }

    @Test
    void testGetLearnerThrowsException() {
        UserClient fallback = fallbackFactory.create(new RuntimeException("User Service Down"));

        DownstreamServiceException exception = assertThrows(
                DownstreamServiceException.class,
                () -> fallback.getLearner("Bearer token", "learner123")
        );

        assertEquals("User service is currently unavailable.", exception.getMessage());
    }
}