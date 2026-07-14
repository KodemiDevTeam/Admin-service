package com.example.admin_service.service.notification;

import com.example.admin_service.dto.notification.BroadcastNotificationRequest;
import com.example.admin_service.dto.notification.NotificationChannel;
import com.example.admin_service.dto.notification.NotificationRequest;
import com.example.admin_service.dto.notification.NotificationType;
import com.example.admin_service.feign.NotificationClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NotificationPublisherTest {

    private NotificationClient notificationClient;
    private NotificationPublisher publisher;

    @BeforeEach
    void setup() {
        notificationClient = mock(NotificationClient.class);
        publisher = new NotificationPublisher(notificationClient);
        ReflectionTestUtils.setField(publisher, "serviceKey", "test-key");
    }

    private NotificationRequest buildRequest() {
        return NotificationRequest.builder()
                .userId("u1")
                .title("Test")
                .message("Hello")
                .type(NotificationType.TRAINER_VERIFIED)
                .channels(List.of(NotificationChannel.EMAIL))
                .referenceId("ref1")
                .referenceType("USER")
                .build();
    }

    private BroadcastNotificationRequest buildBroadcast() {
        return BroadcastNotificationRequest.builder()
                .title("Broadcast")
                .message("All users")
                .type(NotificationType.ADMIN_BROADCAST)
                .channels(List.of(NotificationChannel.IN_APP))
                .referenceId("b1")
                .referenceType("BROADCAST")
                .sendMode("ALL_USERS")
                .build();
    }

    @Test
    void publish_success_callsNotificationClient() {
        NotificationRequest req = buildRequest();

        when(notificationClient.sendInternalNotification(anyString(), any()))
                .thenReturn(Map.of("status", "success"));

        assertDoesNotThrow(() -> publisher.publish(req));

        verify(notificationClient)
                .sendInternalNotification("test-key", req);
    }

    @Test
    void publish_clientThrows_doesNotPropagate() {
        NotificationRequest req = buildRequest();

        when(notificationClient.sendInternalNotification(anyString(), any()))
                .thenThrow(new RuntimeException("feign error"));

        assertDoesNotThrow(() -> publisher.publish(req));
    }

    @Test
    void publishBroadcast_success_callsNotificationClient() {
        BroadcastNotificationRequest req = buildBroadcast();

        when(notificationClient.broadcastNotification(anyString(), any()))
                .thenReturn(Map.of("status", "success"));

        assertDoesNotThrow(() -> publisher.publishBroadcast(req));

        verify(notificationClient)
                .broadcastNotification("test-key", req);
    }

    @Test
    void publishBroadcast_clientThrows_doesNotPropagate() {
        BroadcastNotificationRequest req = buildBroadcast();

        when(notificationClient.broadcastNotification(anyString(), any()))
                .thenThrow(new RuntimeException("feign error"));

        assertDoesNotThrow(() -> publisher.publishBroadcast(req));
    }

    @Test
    void publishToUsers_multipleUsers_sendsToEach() {
        NotificationRequest req = buildRequest();

        when(notificationClient.sendInternalNotification(anyString(), any()))
                .thenReturn(Map.of("status", "success"));

        publisher.publishToUsers(List.of("u1", "u2", "u3"), req);

        verify(notificationClient, times(3))
                .sendInternalNotification(eq("test-key"), any());
    }

    @Test
    void publishToUsers_emptyList_doesNothing() {
        NotificationRequest req = buildRequest();

        assertDoesNotThrow(() -> publisher.publishToUsers(List.of(), req));

        verify(notificationClient, never())
                .sendInternalNotification(anyString(), any());
    }

    @Test
    void publishToUsers_nullList_doesNothing() {
        NotificationRequest req = buildRequest();

        assertDoesNotThrow(() -> publisher.publishToUsers(null, req));

        verify(notificationClient, never())
                .sendInternalNotification(anyString(), any());
    }

    @Test
    void publishToUsers_oneUserThrows_continuesForOthers() {
        NotificationRequest req = buildRequest();

        when(notificationClient.sendInternalNotification(anyString(), any()))
                .thenThrow(new RuntimeException("error"));

        assertDoesNotThrow(() -> publisher.publishToUsers(List.of("u1", "u2"), req));
    }
}