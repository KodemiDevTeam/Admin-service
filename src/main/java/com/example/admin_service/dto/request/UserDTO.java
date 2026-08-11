package com.example.admin_service.dto.request;
import com.example.admin_service.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class UserDTO {
    private String userId;
    private String email;
    private String username;
    private Role role;
}
