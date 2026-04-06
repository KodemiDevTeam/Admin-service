package com.example.admin_service.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleNullPointers() {
        com.example.admin_service.exceptions.NullPointerException ex = new com.example.admin_service.exceptions.NullPointerException("Null error");
        ResponseEntity<Map<String, Object>> response = handler.handleNullPointers(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().get("status"));
        assertEquals("Null error", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), response.getBody().get("error"));
    }

    @Test
    void testHandleInvalidCredentials() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Invalid");
        ResponseEntity<Map<String, Object>> response = handler.handleInvalidCredentials(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getBody().get("status"));
        assertEquals("Invalid", response.getBody().get("message"));
    }

    @Test
    void testHandleNoRequest() {
        NoActiveRequestException ex = new NoActiveRequestException("No active request");
        ResponseEntity<Map<String, Object>> response = handler.handleNoRequest(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().get("status"));
        assertEquals("No active request", response.getBody().get("message"));
    }

    @Test
    void testHandleOtpInvalidToken() {
        InvalidTokenException ex = new InvalidTokenException("Invalid token", null);
        ResponseEntity<Map<String, Object>> response = handler.handleOtpInvalidToken(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().get("status"));
        assertEquals("Invalid token", response.getBody().get("message"));
    }
}
