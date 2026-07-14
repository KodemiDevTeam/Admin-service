package com.example.admin_service.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserSuspendExceptionTest {

    @Test
    void testMessageOnly() {
        UserSuspendException ex = new UserSuspendException("Failed to notify user");
        assertEquals("Failed to notify user", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void testMessageWithCause() {
        RuntimeException cause = new RuntimeException("notification down");
        UserSuspendException ex = new UserSuspendException("Failed to notify user", cause);
        assertEquals("Failed to notify user", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void testIsRuntimeException() {
        UserSuspendException ex = new UserSuspendException("test");
        assertInstanceOf(RuntimeException.class, ex);
    }
}
