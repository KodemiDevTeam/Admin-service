package com.example.admin_service.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DownstreamServiceExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Service unavailable";

        DownstreamServiceException exception =
                new DownstreamServiceException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Service unavailable";
        Throwable cause = new RuntimeException("Connection refused");

        DownstreamServiceException exception =
                new DownstreamServiceException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}