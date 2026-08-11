package com.example.admin_service.dto.notification;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    private String recipient;
    private String userId;
    private String title;
    private String message;
    private NotificationType type; // or String, depending on your enum definition
    private List<NotificationChannel> channels;
    private String referenceId;
    private String referenceType;
}