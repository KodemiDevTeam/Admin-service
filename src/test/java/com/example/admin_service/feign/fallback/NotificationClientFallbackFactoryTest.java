package com.example.admin_service.feign.fallback;

import com.example.admin_service.dto.request.BroadcastNotificationRequest;
import com.example.admin_service.dto.request.NotificationRequest;
import com.example.admin_service.exceptions.DownstreamServiceException;
import com.example.admin_service.feign.NotificationClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationClientFallbackFactoryTest {

    private NotificationClientFallbackFactory fallbackFactory;
    private NotificationClient fallbackClient;

    @BeforeEach
    void setUp() {
        fallbackFactory = new NotificationClientFallbackFactory();
        fallbackClient = fallbackFactory.create(new RuntimeException("Service unavailable"));
    }

    @Test
    void testSendInternalNotificationThrowsDownstreamServiceException() {
        assertThrows(DownstreamServiceException.class, () -> {
            fallbackClient.sendInternalNotification("token", null);
        });
    }

    @Test
    void testBroadcastNotificationThrowsDownstreamServiceException() {
        assertThrows(DownstreamServiceException.class, () -> {
            fallbackClient.broadcastNotification("token", null);
        });
    }

    @Test
    void testFallbackClientImplementsNotificationClient() {
        assertTrue(fallbackClient instanceof NotificationClient);
    }

    @Test
    void testFallbackFactoryCreateWithDifferentCauses() {
        NotificationClient client1 = fallbackFactory.create(new RuntimeException("Error 1"));
        NotificationClient client2 = fallbackFactory.create(new IllegalStateException("Error 2"));

        assertNotNull(client1);
        assertNotNull(client2);
    }

    @Test
    void testMultipleFallbackCreations() {
        NotificationClient fallback1 = fallbackFactory.create(new RuntimeException("Error 1"));
        NotificationClient fallback2 = fallbackFactory.create(new RuntimeException("Error 2"));

        assertNotNull(fallback1);
        assertNotNull(fallback2);
        assertNotSame(fallback1, fallback2);
    }

    @Test
    void testFallbackWithNullCause() {
        NotificationClient client = fallbackFactory.create(null);
        assertNotNull(client);
        assertThrows(DownstreamServiceException.class, () -> client.sendInternalNotification("token", null));
    }

    @Test
    void testSendInternalNotificationWithNullCause() {
        NotificationClient client = fallbackFactory.create(null);
        NotificationRequest request = NotificationRequest.builder()
                .userId("user123")
                .title("Test")
                .message("Test message")
                .build();
        
        DownstreamServiceException exception = assertThrows(DownstreamServiceException.class, () ->
            client.sendInternalNotification("token", request)
        );
        
        assertNotNull(exception.getMessage());
    }

    @Test
    void testBroadcastNotificationWithNullCause() {
        NotificationClient client = fallbackFactory.create(null);
        BroadcastNotificationRequest request = BroadcastNotificationRequest.builder()
                .title("Test")
                .message("Test message")
                .build();
        
        DownstreamServiceException exception = assertThrows(DownstreamServiceException.class, () ->
            client.broadcastNotification("token", request)
        );
        
        assertNotNull(exception.getMessage());
    }

    @Test
    void testExceptionIncludesCause() {
        RuntimeException cause = new RuntimeException("Original error");
        NotificationClient client = fallbackFactory.create(cause);
        NotificationRequest request = NotificationRequest.builder().build();
        
        DownstreamServiceException exception = assertThrows(DownstreamServiceException.class, () ->
            client.sendInternalNotification("token", request)
        );
        
        assertSame(cause, exception.getCause());
    }

    @Test
    void testSendInternalNotificationExceptionMessage() {
        NotificationRequest request = NotificationRequest.builder().build();
        DownstreamServiceException exception = assertThrows(DownstreamServiceException.class, () -> {
            fallbackClient.sendInternalNotification("token", request);
        });

        assertTrue(exception.getMessage().contains("unavailable"));
    }

    @Test
    void testBroadcastNotificationExceptionMessage() {
        BroadcastNotificationRequest request = BroadcastNotificationRequest.builder().build();
        DownstreamServiceException exception = assertThrows(DownstreamServiceException.class, () -> {
            fallbackClient.broadcastNotification("token", request);
        });

        assertTrue(exception.getMessage().contains("unavailable"));
    }

    @Test
    void testAllMethodsThrowDownstreamServiceException() {
        NotificationRequest internalRequest = NotificationRequest.builder().build();
        BroadcastNotificationRequest broadcastRequest = BroadcastNotificationRequest.builder().build();
        assertThrows(DownstreamServiceException.class, () -> fallbackClient.sendInternalNotification("token", internalRequest));
        assertThrows(DownstreamServiceException.class, () -> fallbackClient.broadcastNotification("token", broadcastRequest));
    }
}
