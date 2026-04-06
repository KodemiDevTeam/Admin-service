package com.example.admin_service.exceptions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InvalidCredentialsExceptionTest {
    @Test
    void testMessageConstructor() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Invalid Credentials");
        assertEquals("Invalid Credentials", ex.getMessage());
    }
}
