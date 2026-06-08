package com.example.admin_service.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BroadcastNotificationRequest {
    private String title;
    private String message;
    private String type;
    private List<String> channels;
    private List<String> targetRoles;
    private String referenceId;
}

