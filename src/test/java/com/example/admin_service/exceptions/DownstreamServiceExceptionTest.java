package com.example.admin_service.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DownstreamServiceExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Service unavailable";
        DownstreamServiceException exception = new DownstreamServiceException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Service unavailable";
        RuntimeException cause = new RuntimeException("Original error");
        DownstreamServiceException exception = new DownstreamServiceException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void testIsRuntimeException() {
        DownstreamServiceException exception = new DownstreamServiceException("Test");
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testThrowAndCatch() {
        assertThrows(DownstreamServiceException.class, () -> {
            throw new DownstreamServiceException("Test error");
        });
    }

    @Test
    void testExceptionWithNullMessage() {
        DownstreamServiceException exception = new DownstreamServiceException(null);
        assertNull(exception.getMessage());
    }

    @Test
    void testExceptionWithNullCause() {
        DownstreamServiceException exception = new DownstreamServiceException("Message", null);
        assertEquals("Message", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testExceptionCauseChain() {
        Exception rootCause = new Exception("Root cause");
        RuntimeException intermediateCause = new RuntimeException("Intermediate", rootCause);
        DownstreamServiceException exception = new DownstreamServiceException("Service error", intermediateCause);
        
        assertSame(intermediateCause, exception.getCause());
        assertSame(rootCause, exception.getCause().getCause());
    }

    @Test
    void testExceptionStackTrace() {
        DownstreamServiceException exception = new DownstreamServiceException("Test");
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
    }

    @Test
    void testExceptionToString() {
        DownstreamServiceException exception = new DownstreamServiceException("Test message");
        String exceptionString = exception.toString();
        
        assertNotNull(exceptionString);
        assertTrue(exceptionString.contains("DownstreamServiceException"));
        assertTrue(exceptionString.contains("Test message"));
    }

    @Test
    void testMultipleExceptions() {
        DownstreamServiceException exception1 = new DownstreamServiceException("Error 1");
        DownstreamServiceException exception2 = new DownstreamServiceException("Error 2");
        
        assertNotEquals(exception1.getMessage(), exception2.getMessage());
    }
}
