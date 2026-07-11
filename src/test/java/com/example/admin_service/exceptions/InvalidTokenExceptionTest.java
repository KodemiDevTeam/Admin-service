package com.example.admin_service.exceptions;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvalidTokenExceptionTest {

    @Test
    void testMessageConstructor_withNullCause() {
        InvalidTokenException ex = new InvalidTokenException("Invalid Token", null);
        assertEquals("Invalid Token", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void testMessageConstructor_withExpiredJwtException() {
        ExpiredJwtException cause = new ExpiredJwtException(null, null, "Expired");
        InvalidTokenException ex = new InvalidTokenException("Token expired", cause);

        assertEquals("Token expired", ex.getMessage());
        assertSame(cause, ex.getCause(), "Cause should be stored and retrievable");
    }
}
