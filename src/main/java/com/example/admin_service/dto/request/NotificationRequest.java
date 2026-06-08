package com.example.admin_service.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NotificationRequest {
    private String userId;
    private String title;
    private String message;
    private String type;
    private List<String> channels;
    private String referenceId;
}

