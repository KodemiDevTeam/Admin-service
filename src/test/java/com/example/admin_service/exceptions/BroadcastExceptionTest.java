package com.example.admin_service.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BroadcastExceptionTest {

    @Test
    void testMessageOnly() {
        BroadcastException ex = new BroadcastException("Rate limit exceeded");
        assertEquals("Rate limit exceeded", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void testMessageWithCause() {
        RuntimeException cause = new RuntimeException("kafka down");
        BroadcastException ex = new BroadcastException("Failed to broadcast", cause);
        assertEquals("Failed to broadcast", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void testIsRuntimeException() {
        BroadcastException ex = new BroadcastException("test");
        assertInstanceOf(RuntimeException.class, ex);
    }
}
