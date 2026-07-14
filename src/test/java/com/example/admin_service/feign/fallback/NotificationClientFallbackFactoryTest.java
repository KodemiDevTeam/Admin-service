package com.example.admin_service.feign.fallback;

import com.example.admin_service.dto.notification.BroadcastNotificationRequest;
import com.example.admin_service.dto.notification.NotificationRequest;
import com.example.admin_service.feign.NotificationClient;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NotificationClientFallbackFactoryTest {

    private final NotificationClientFallbackFactory fallbackFactory =
            new NotificationClientFallbackFactory();

    @Test
    void testCreateReturnsFallbackInstance() {
        Throwable cause = new RuntimeException("Notification Service Down");

        NotificationClient fallback = fallbackFactory.create(cause);

        assertNotNull(fallback);
    }

    @Test
    void testSendInternalNotificationFallback() {
        Throwable cause = new RuntimeException("Notification Service Down");
        NotificationClient fallback = fallbackFactory.create(cause);

        NotificationRequest request = NotificationRequest.builder()
                .title("Test Title")
                .message("Test Message")
                .build();

        Map<String, String> response =
                fallback.sendInternalNotification("Bearer token", request);

        assertNotNull(response);
        assertEquals(Collections.emptyMap(), response);
        assertTrue(response.isEmpty());
    }

    @Test
    void testBroadcastNotificationFallback() {
        Throwable cause = new RuntimeException("Notification Service Down");
        NotificationClient fallback = fallbackFactory.create(cause);

        BroadcastNotificationRequest request = BroadcastNotificationRequest.builder()
                .title("Broadcast Title")
                .message("Broadcast Message")
                .build();

        Map<String, String> response =
                fallback.broadcastNotification("Bearer token", request);

        assertNotNull(response);
        assertEquals(Collections.emptyMap(), response);
        assertTrue(response.isEmpty());
    }
}