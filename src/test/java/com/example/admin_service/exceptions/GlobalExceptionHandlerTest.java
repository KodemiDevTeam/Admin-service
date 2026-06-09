package com.example.admin_service.exceptions;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleNullPointerException() {
        NullPointerException ex = new NullPointerException("Null value found");
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleNullPointers(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().get("status"));
        assertEquals("Null value found", response.getBody().get("message"));
    }

    @Test
    void testHandleInvalidCredentialsException() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Invalid credentials provided");
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleInvalidCredentials(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().get("status"));
        assertEquals("Invalid credentials provided", response.getBody().get("message"));
    }

    @Test
    void testHandleNoActiveRequestException() {
        NoActiveRequestException ex = new NoActiveRequestException("No active request");
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleNoRequest(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().get("status"));
    }

    @Test
    void testHandleInvalidTokenException() {
        InvalidTokenException ex = new InvalidTokenException("Token expired", null);
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleOtpInvalidToken(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
    }

    @Test
    void testHandleFetchPendingPayoutException() {
        FetchPendingPayoutException ex = new FetchPendingPayoutException("Failed to fetch payouts");
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleFetchPendingPayout(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
    }

    @Test
    void testHandleInvalidPayoutActionException() {
        InvalidPayoutActionException ex = new InvalidPayoutActionException("Invalid action: UNKNOWN");
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleInvalidCredentials(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
    }

    @Test
    void testHandlePaymentClientException() {
        PaymentClientException ex = new PaymentClientException("Payment service error");
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handlePaymentClientException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
    }

    @Test
    void testHandleUnauthorizedPayoutAccessException() {
        UnauthorizedPayoutAccessException ex = new UnauthorizedPayoutAccessException("Unauthorized access");
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleUnauthorizedPayoutAccessException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().get("status"));
    }

    @Test
    void testHandlePayoutProcessingException() {
        PayoutProcessingException ex = new PayoutProcessingException("Processing failed");
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handlePayoutProcessingException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
    }

    @Test
    void testHandleDownstreamServiceFailuresFeignException() {
        FeignException ex = new FeignException.InternalServerError("Server error", null, null);
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleDownstreamServiceFailures(ex);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(503, response.getBody().get("status"));
        assertTrue(response.getBody().get("message").toString().contains("unavailable"));
    }

    @Test
    void testHandleDownstreamServiceFailuresConnectException() {
        ConnectException ex = new ConnectException("Connection refused");
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleDownstreamServiceFailures(ex);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(503, response.getBody().get("status"));
    }

    @Test
    void testHandleDownstreamServiceFailuresSocketTimeoutException() {
        SocketTimeoutException ex = new SocketTimeoutException("Socket timeout");
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleDownstreamServiceFailures(ex);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(503, response.getBody().get("status"));
    }

    @Test
    void testHandleGenericException() {
        Exception ex = new Exception("Generic error");
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().get("status"));
        assertTrue(response.getBody().get("message").toString().contains("internal server error"));
    }

    @Test
    void testErrorResponseHasTimestamp() {
        NullPointerException ex = new NullPointerException("Test");
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleNullPointers(ex);

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().get("timestamp"));
        assertTrue(response.getBody().get("timestamp").toString().length() > 0);
    }

    @Test
    void testErrorResponseHasAllFields() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Test error");
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleInvalidCredentials(ex);

        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("timestamp"));
        assertTrue(response.getBody().containsKey("status"));
        assertTrue(response.getBody().containsKey("error"));
        assertTrue(response.getBody().containsKey("message"));
    }
}
