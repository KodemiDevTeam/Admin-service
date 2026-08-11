package com.example.admin_service.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class AdminBroadcastRequest {
    private String title;
    private String message;
    private boolean urgent;
    private List<String> channels;
    private String targetRole;
}
