package com.example.admin_service.dto.response;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AdminLoginRequestTest {

    @Test
    void testGettersAndSetters() {
        AdminLoginRequest dto = new AdminLoginRequest();
        dto.setAdminId("a1");
        dto.setUserId("u1");
        dto.setEmail("test@email.com");
        dto.setUsername("testuser");
        dto.setAdminRole(null);

        assertEquals("a1", dto.getAdminId());
        assertEquals("u1", dto.getUserId());
        assertEquals("test@email.com", dto.getEmail());
        assertEquals("testuser", dto.getUsername());
        assertNull(dto.getAdminRole());
    }
}
