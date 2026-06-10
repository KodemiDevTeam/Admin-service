package com.example.admin_service.feign.fallback;

import com.example.admin_service.exceptions.DownstreamServiceException;
import com.example.admin_service.feign.UserClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserClientFallbackFactoryTest {

    private UserClientFallbackFactory fallbackFactory;
    private UserClient fallbackClient;

    @BeforeEach
    void setUp() {
        fallbackFactory = new UserClientFallbackFactory();
        fallbackClient = fallbackFactory.create(new RuntimeException("Service unavailable"));
    }

    @Test
    void testGetTrainerByIdThrowsDownstreamServiceException() {
        assertThrows(DownstreamServiceException.class, () -> {
            fallbackClient.getTrainerById("token", "trainerId");
        });
    }

    @Test
    void testGetAllTrainersThrowsDownstreamServiceException() {
        assertThrows(DownstreamServiceException.class, () -> {
            fallbackClient.getAllTrainers("token");
        });
    }

    @Test
    void testGetAdminThrowsDownstreamServiceException() {
        assertThrows(DownstreamServiceException.class, () -> {
            fallbackClient.getAdmin("adminId");
        });
    }

    @Test
    void testGetAllPendingTrainersThrowsDownstreamServiceException() {
        assertThrows(DownstreamServiceException.class, () -> {
            fallbackClient.getAllPendingTrainers("token");
        });
    }

    @Test
    void testGetLearnerThrowsDownstreamServiceException() {
        assertThrows(DownstreamServiceException.class, () -> {
            fallbackClient.getLearner("token", "learnerId");
        });
    }

    @Test
    void testFallbackClientImplementsUserClient() {
        assertTrue(fallbackClient instanceof UserClient);
    }

    @Test
    void testFallbackWithNullCause() {
        UserClient client = fallbackFactory.create(null);
        assertNotNull(client);
        assertThrows(DownstreamServiceException.class, () -> client.getAllTrainers("token"));
    }

    @Test
    void testGetTrainerByIdWithNullCause() {
        UserClient client = fallbackFactory.create(null);
        
        DownstreamServiceException exception = assertThrows(DownstreamServiceException.class, () -> {
            client.getTrainerById("token", "trainerId");
        });
        
        assertNotNull(exception.getMessage());
    }

    @Test
    void testGetAdminWithNullCause() {
        UserClient client = fallbackFactory.create(null);
        
        DownstreamServiceException exception = assertThrows(DownstreamServiceException.class, () -> {
            client.getAdmin("adminId");
        });
        
        assertNotNull(exception.getMessage());
    }

    @Test
    void testGetAllPendingTrainersWithNullCause() {
        UserClient client = fallbackFactory.create(null);
        
        DownstreamServiceException exception = assertThrows(DownstreamServiceException.class, () -> {
            client.getAllPendingTrainers("token");
        });
        
        assertNotNull(exception.getMessage());
    }

    @Test
    void testGetLearnerWithNullCause() {
        UserClient client = fallbackFactory.create(null);
        
        DownstreamServiceException exception = assertThrows(DownstreamServiceException.class, () -> {
            client.getLearner("token", "learnerId");
        });
        
        assertNotNull(exception.getMessage());
    }

    @Test
    void testExceptionIncludesCause() {
        RuntimeException cause = new RuntimeException("Original error");
        UserClient client = fallbackFactory.create(cause);
        
        DownstreamServiceException exception = assertThrows(DownstreamServiceException.class, () -> {
            client.getAllTrainers("token");
        });
        
        assertSame(cause, exception.getCause());
    }

    @Test
    void testAllMethodsThrowDownstreamServiceException() {
        assertThrows(DownstreamServiceException.class, () -> fallbackClient.getTrainerById("token", "id"));
        assertThrows(DownstreamServiceException.class, () -> fallbackClient.getAllTrainers("token"));
        assertThrows(DownstreamServiceException.class, () -> fallbackClient.getAdmin("adminId"));
        assertThrows(DownstreamServiceException.class, () -> fallbackClient.getAllPendingTrainers("token"));
        assertThrows(DownstreamServiceException.class, () -> fallbackClient.getLearner("token", "learnerId"));
    }

    @Test
    void testMultipleFallbackCreations() {
        UserClient fallback1 = fallbackFactory.create(new RuntimeException("Error 1"));
        UserClient fallback2 = fallbackFactory.create(new RuntimeException("Error 2"));

        assertNotNull(fallback1);
        assertNotNull(fallback2);
        assertNotSame(fallback1, fallback2);
    }

    @Test
    void testFallbackFactoryCreateWithDifferentCauses() {
        UserClient client1 = fallbackFactory.create(new RuntimeException("Error 1"));
        UserClient client2 = fallbackFactory.create(new IllegalStateException("Error 2"));

        assertNotNull(client1);
        assertNotNull(client2);
        DownstreamServiceException exception1 = assertThrows(DownstreamServiceException.class, () -> client1.getAllTrainers("token"));
        assertNotNull(exception1);
        DownstreamServiceException exception2 = assertThrows(DownstreamServiceException.class, () -> client2.getAllTrainers("token"));
        assertNotNull(exception2);
    }

    @Test
    void testExceptionMessageContainsServiceUnavailable() {
        DownstreamServiceException exception = assertThrows(DownstreamServiceException.class, () -> {
            fallbackClient.getAllTrainers("token");
        });

        assertTrue(exception.getMessage().contains("unavailable"));
    }
}
