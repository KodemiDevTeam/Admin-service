package com.example.admin_service.dto.response;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AdminResponseDTOTest {

    @Test
    void testGettersSettersAndConstructors() {
        AdminResponseDTO dto = new AdminResponseDTO();
        dto.setAdminId("a1");
        dto.setUserId("u1");
        dto.setEmail("test@email.com");
        dto.setUsername("testuser");
        dto.setAdminRole(null);

        assertEquals("a1", dto.getAdminId());

        AdminResponseDTO allArgsDto = new AdminResponseDTO("a2", "u2", "user2@email.com", "user2", null);
        assertEquals("a2", allArgsDto.getAdminId());

        AdminResponseDTO builderDto = AdminResponseDTO.builder()
                .adminId("a3")
                .userId("u3")
                .email("user3@email.com")
                .username("user3")
                .adminRole(null)
                .build();
        assertEquals("a3", builderDto.getAdminId());
    }
}
