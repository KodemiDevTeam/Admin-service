
        package com.example.admin_service.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private static final String STATUS = "status";
    private static final String MESSAGE = "message";
    private static final String ERROR = "error";
    private static final String TIMESTAMP = "timestamp";

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    // ===== NULL POINTER =====
    @Test
    void testHandleNullPointersfullCoverage() {
        NullPointerException ex = new NullPointerException("Null error");

        ResponseEntity<Map<String, Object>> response =
                handler.handleNullPointers(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        assertEquals(HttpStatus.NOT_FOUND.value(), body.get(STATUS));
        assertEquals("Null error", body.get(MESSAGE));
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), body.get(ERROR));
        assertNotNull(body.get(TIMESTAMP));
    }

    // ===== INVALID CREDENTIALS =====
    @Test
    void testHandleInvalidCredentialsfullCoverage() {
        InvalidCredentialsException ex =
                new InvalidCredentialsException("Invalid credentials");

        ResponseEntity<Map<String, Object>> response =
                handler.handleInvalidCredentials(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), body.get(STATUS));
        assertEquals("Invalid credentials", body.get(MESSAGE));
        assertEquals(
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                body.get(ERROR)
        );
        assertNotNull(body.get(TIMESTAMP));
    }

    @Test
    void testHandleInvalidCredentialsnullMessage() {
        InvalidCredentialsException ex =
                new InvalidCredentialsException(null);

        ResponseEntity<Map<String, Object>> response =
                handler.handleInvalidCredentials(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        assertNull(body.get(MESSAGE));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), body.get(STATUS));
        assertEquals(
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                body.get(ERROR)
        );
        assertNotNull(body.get(TIMESTAMP));
    }

    // ===== NO ACTIVE REQUEST =====
    @Test
    void testHandleNoRequestfullCoverage() {
        NoActiveRequestException ex =
                new NoActiveRequestException("No active request");

        ResponseEntity<Map<String, Object>> response =
                handler.handleNoRequest(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        assertEquals(HttpStatus.NOT_FOUND.value(), body.get(STATUS));
        assertEquals("No active request", body.get(MESSAGE));
        assertEquals(
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                body.get(ERROR)
        );
        assertNotNull(body.get(TIMESTAMP));
    }

    // ===== INVALID TOKEN =====
    @Test
    void testHandleInvalidTokenfullCoverage() {
        InvalidTokenException ex =
                new InvalidTokenException("Invalid token", null);

        ResponseEntity<Map<String, Object>> response =
                handler.handleOtpInvalidToken(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        assertEquals(HttpStatus.BAD_REQUEST.value(), body.get(STATUS));
        assertEquals("Invalid token", body.get(MESSAGE));
        assertEquals(
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                body.get(ERROR)
        );
        assertNotNull(body.get(TIMESTAMP));
    }

    @Test
    void testHandleInvalidTokennullMessage() {
        InvalidTokenException ex =
                new InvalidTokenException(null, null);

        ResponseEntity<Map<String, Object>> response =
                handler.handleOtpInvalidToken(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        assertNull(body.get(MESSAGE));
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.get(STATUS));
        assertEquals(
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                body.get(ERROR)
        );
        assertNotNull(body.get(TIMESTAMP));
    }

    // ===== FETCH PENDING PAYOUT =====
    @Test
    void testHandleFetchPendingPayoutfullCoverage() {
        FetchPendingPayoutException ex =
                new FetchPendingPayoutException("Fetch failed");

        ResponseEntity<Map<String, Object>> response =
                handler.handleFetchPendingPayout(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        assertEquals(HttpStatus.BAD_REQUEST.value(), body.get(STATUS));
        assertEquals("Fetch failed", body.get(MESSAGE));
        assertEquals(
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                body.get(ERROR)
        );
        assertNotNull(body.get(TIMESTAMP));
    }

    @Test
    void testHandleFetchPendingPayoutnullMessage() {
        FetchPendingPayoutException ex =
                new FetchPendingPayoutException(null);

        ResponseEntity<Map<String, Object>> response =
                handler.handleFetchPendingPayout(ex);

        assertNull(response.getBody().get(MESSAGE));
    }

    // ===== INVALID PAYOUT ACTION =====
    @Test
    void testHandleInvalidPayoutActionfullCoverage() {
        InvalidPayoutActionException ex =
                new InvalidPayoutActionException("Wrong action");

        ResponseEntity<Map<String, Object>> response =
                handler.handleInvalidCredentials(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        assertEquals("Wrong action", body.get(MESSAGE));
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.get(STATUS));
        assertNotNull(body.get(TIMESTAMP));
    }

    @Test
    void testHandleInvalidPayoutActionnullMessage() {
        InvalidPayoutActionException ex =
                new InvalidPayoutActionException(null);

        ResponseEntity<Map<String, Object>> response =
                handler.handleInvalidCredentials(ex);

        assertNull(response.getBody().get(MESSAGE));
    }

    // ===== PAYMENT CLIENT EXCEPTION =====
    @Test
    void testHandlePaymentClientExceptionfullCoverage() {
        PaymentClientException ex =
                new PaymentClientException("Payment failed");

        ResponseEntity<Map<String, Object>> response =
                handler.handlePaymentClientException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        assertEquals("Payment failed", body.get(MESSAGE));
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.get(STATUS));
        assertNotNull(body.get(TIMESTAMP));
    }

    @Test
    void testHandlePaymentClientExceptionnullMessage() {
        PaymentClientException ex =
                new PaymentClientException(null);

        ResponseEntity<Map<String, Object>> response =
                handler.handlePaymentClientException(ex);

        assertNull(response.getBody().get(MESSAGE));
    }

    // ===== UNAUTHORIZED PAYOUT ACCESS =====
    @Test
    void testHandleUnauthorizedPayoutAccessfullCoverage() {
        UnauthorizedPayoutAccessException ex =
                new UnauthorizedPayoutAccessException("Unauthorized");

        ResponseEntity<Map<String, Object>> response =
                handler.handleUnauthorizedPayoutAccessException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        assertEquals("Unauthorized", body.get(MESSAGE));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), body.get(STATUS));
        assertNotNull(body.get(TIMESTAMP));
    }

    @Test
    void testHandleUnauthorizedPayoutAccessnullMessage() {
        UnauthorizedPayoutAccessException ex =
                new UnauthorizedPayoutAccessException(null);

        ResponseEntity<Map<String, Object>> response =
                handler.handleUnauthorizedPayoutAccessException(ex);

        assertNull(response.getBody().get(MESSAGE));
    }

    // ===== PAYOUT PROCESSING =====
    @Test
    void testHandlePayoutProcessingExceptionfullCoverage() {
        PayoutProcessingException ex =
                new PayoutProcessingException("Processing failed");

        ResponseEntity<Map<String, Object>> response =
                handler.handlePayoutProcessingException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        assertEquals("Processing failed", body.get(MESSAGE));
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.get(STATUS));
        assertNotNull(body.get(TIMESTAMP));
    }

    @Test
    void testHandlePayoutProcessingExceptionnullMessage() {
        PayoutProcessingException ex =
                new PayoutProcessingException(null);

        ResponseEntity<Map<String, Object>> response =
                handler.handlePayoutProcessingException(ex);

        assertNull(response.getBody().get(MESSAGE));
    }
}

