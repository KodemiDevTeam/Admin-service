package com.example.admin_service.dto.request;


import lombok.*;

@Getter
@Setter
public class AdminLoginDTO {
    private String adminId;
    private String password;
}
