package com.example.admin_service.dto.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SubAdminRequestTest {

    @Test
    void testGetters() {
        SubAdminRequest dto = new SubAdminRequest();
        // Since there is only @Getter, the fields will be initialized to null
        assertNull(dto.getAdminId());
        assertNull(dto.getPassword());
        assertNull(dto.getAdminRole());
    }
}
