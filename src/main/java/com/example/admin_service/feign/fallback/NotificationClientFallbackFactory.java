package com.example.admin_service.feign.fallback;

import com.example.admin_service.feign.NotificationClient;
import com.example.admin_service.dto.request.BroadcastNotificationRequest;
import com.example.admin_service.dto.request.NotificationRequest;
import com.example.admin_service.exceptions.DownstreamServiceException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@Slf4j
@Component
public class NotificationClientFallbackFactory implements FallbackFactory<NotificationClient> {
    private static final String UNKNOWN_ERROR = "Unknown error";

    @Override
    public NotificationClient create(Throwable cause) {
        return new NotificationClient() {
            @Override
            public Map<String, String> sendInternalNotification(String token, NotificationRequest request) {
                String errorMessage = cause != null ? cause.getMessage() : UNKNOWN_ERROR;
                log.error("NotificationClient sendInternalNotification failed: {}", errorMessage, cause);
                throw new DownstreamServiceException("Notification service is currently unavailable.", cause);
            }

            @Override
            public Map<String, String> broadcastNotification(String token, BroadcastNotificationRequest request) {
                String errorMessage = cause != null ? cause.getMessage() : UNKNOWN_ERROR;
                log.error("NotificationClient broadcastNotification failed: {}", errorMessage, cause);
                throw new DownstreamServiceException("Notification service is currently unavailable.", cause);
            }
        };
    }
}
