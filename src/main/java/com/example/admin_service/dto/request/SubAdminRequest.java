package com.example.admin_service.dto.request;

import com.example.admin_service.enums.AdminRole;
import lombok.Getter;

@Getter
public class SubAdminRequest {
    private String adminId;
    private String password;
    private AdminRole adminRole;
}
