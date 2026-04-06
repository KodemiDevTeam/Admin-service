package com.example.admin_service.exceptions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NullPointerExceptionTest {
    @Test
    void testMessageConstructor() {
        com.example.admin_service.exceptions.NullPointerException ex = 
            new com.example.admin_service.exceptions.NullPointerException("Null Pointer");
        assertEquals("Null Pointer", ex.getMessage());
    }
}
