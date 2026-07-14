package com.example.admin_service.feign.fallback;

import com.example.admin_service.dto.request.ProcessPayoutRequest;
import com.example.admin_service.exceptions.DownstreamServiceException;
import com.example.admin_service.feign.PaymentClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentClientFallbackFactoryTest {

    private final PaymentClientFallbackFactory fallbackFactory =
            new PaymentClientFallbackFactory();

    @Test
    void testCreateReturnsFallbackInstance() {
        Throwable cause = new RuntimeException("Payment Service Down");

        PaymentClient fallback = fallbackFactory.create(cause);

        assertNotNull(fallback);
    }

    @Test
    void testGetAllPayoutsThrowsException() {
        Throwable cause = new RuntimeException("Payment Service Down");
        PaymentClient fallback = fallbackFactory.create(cause);

        DownstreamServiceException exception = assertThrows(
                DownstreamServiceException.class,
                () -> fallback.getAllPayouts("Bearer token")
        );

        assertEquals("Payment service is currently unavailable.", exception.getMessage());
    }

    @Test
    void testProcessPayoutRequestThrowsException() {
        Throwable cause = new RuntimeException("Payment Service Down");
        PaymentClient fallback = fallbackFactory.create(cause);

        ProcessPayoutRequest request = new ProcessPayoutRequest();

        DownstreamServiceException exception = assertThrows(
                DownstreamServiceException.class,
                () -> fallback.processPayoutRequest(request, "Bearer token")
        );

        assertEquals("Payment service is currently unavailable.", exception.getMessage());
    }

    @Test
    void testProcessPayoutRequestByPathThrowsException() {
        Throwable cause = new RuntimeException("Payment Service Down");
        PaymentClient fallback = fallbackFactory.create(cause);

        DownstreamServiceException exception = assertThrows(
                DownstreamServiceException.class,
                () -> fallback.processPayoutRequestByPath(
                        "Bearer token",
                        "APPROVE",
                        "PAYOUT123",
                        "Approved"
                )
        );

        assertEquals("Payment service is currently unavailable.", exception.getMessage());
    }

    @Test
    void testGetTransactionHistoryThrowsException() {
        Throwable cause = new RuntimeException("Payment Service Down");
        PaymentClient fallback = fallbackFactory.create(cause);

        DownstreamServiceException exception = assertThrows(
                DownstreamServiceException.class,
                fallback::getTransactionHistory
        );

        assertEquals("Payment service is currently unavailable.", exception.getMessage());
    }
}