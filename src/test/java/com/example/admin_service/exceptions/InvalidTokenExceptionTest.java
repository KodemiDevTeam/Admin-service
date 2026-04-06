package com.example.admin_service.exceptions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InvalidTokenExceptionTest {
    @Test
    void testMessageConstructor() {
        InvalidTokenException ex = new InvalidTokenException("Invalid Token", null);
        assertEquals("Invalid Token", ex.getMessage());
    }
}
