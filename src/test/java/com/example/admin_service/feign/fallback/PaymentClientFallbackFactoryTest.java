package com.example.admin_service.feign.fallback;

import com.example.admin_service.dto.request.ProcessPayoutRequest;
import com.example.admin_service.dto.response.TransactionHistoryResponse;
import com.example.admin_service.exceptions.DownstreamServiceException;
import com.example.admin_service.feign.PaymentClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentClientFallbackFactoryTest {

    private PaymentClientFallbackFactory fallbackFactory;
    private PaymentClient fallbackClient;

    @BeforeEach
    void setUp() {
        fallbackFactory = new PaymentClientFallbackFactory();
        fallbackClient = fallbackFactory.create(new RuntimeException("Service unavailable"));
    }

    @Test
    void testGetAllPayoutsThrowsDownstreamServiceException() {
        assertThrows(DownstreamServiceException.class, () -> {
            fallbackClient.getAllPayouts("token");
        });
    }

    @Test
    void testGetAllPayoutsErrorMessage() {
        DownstreamServiceException exception = assertThrows(DownstreamServiceException.class, () -> {
            fallbackClient.getAllPayouts("token");
        });

        assertTrue(exception.getMessage().contains("unavailable"));
    }

    @Test
    void testProcessPayoutRequestThrowsDownstreamServiceException() {
        ProcessPayoutRequest request = new ProcessPayoutRequest();
        request.setPayoutId("p1");
        request.setAction("APPROVE");

        assertThrows(DownstreamServiceException.class, () -> {
            fallbackClient.processPayoutRequest(request, "token");
        });
    }

    @Test
    void testProcessPayoutRequestByPathThrowsDownstreamServiceException() {
        assertThrows(DownstreamServiceException.class, () -> {
            fallbackClient.processPayoutRequestByPath("token", "APPROVE", "payout123", "remarks");
        });
    }

    @Test
    void testGetTransactionHistoryThrowsDownstreamServiceException() {
        assertThrows(DownstreamServiceException.class, () -> {
            fallbackClient.getTransactionHistory();
        });
    }

    @Test
    void testFallbackFactoryCreateWithDifferentCauses() {
        PaymentClientFallbackFactory factory = new PaymentClientFallbackFactory();
        PaymentClient client1 = factory.create(new RuntimeException("Error 1"));
        PaymentClient client2 = factory.create(new IllegalStateException("Error 2"));

        assertNotNull(client1);
        assertNotNull(client2);
        DownstreamServiceException exception1 = assertThrows(DownstreamServiceException.class, () -> client1.getAllPayouts("token"));
        assertNotNull(exception1);
        DownstreamServiceException exception2 = assertThrows(DownstreamServiceException.class, () -> client2.getAllPayouts("token"));
        assertNotNull(exception2);
    }

    @Test
    void testFallbackClientImplementsPaymentClient() {
        assertTrue(fallbackClient instanceof PaymentClient);
    }

    @Test
    void testAllFallbackMethodsThrowDownstreamServiceException() {
        // Test all methods throw the same type of exception
        assertThrows(DownstreamServiceException.class, () -> fallbackClient.getAllPayouts("token"));
        assertThrows(DownstreamServiceException.class, () -> fallbackClient.processPayoutRequest(new ProcessPayoutRequest(), "token"));
        assertThrows(DownstreamServiceException.class, () -> fallbackClient.processPayoutRequestByPath("token", "APPROVE", "id", "remarks"));
        assertThrows(DownstreamServiceException.class, () -> fallbackClient.getTransactionHistory());
    }

    @Test
    void testFallbackWithNullCause() {
        PaymentClient client = fallbackFactory.create(null);
        assertNotNull(client);
        assertThrows(DownstreamServiceException.class, () -> client.getAllPayouts("token"));
    }

    @Test
    void testMultipleFallbackCreations() {
        PaymentClient fallback1 = fallbackFactory.create(new RuntimeException("Error 1"));
        PaymentClient fallback2 = fallbackFactory.create(new RuntimeException("Error 2"));

        assertNotNull(fallback1);
        assertNotNull(fallback2);
        assertNotSame(fallback1, fallback2);
    }

    @Test
    void testExceptionMessagePreservesServiceUnavailableContext() {
        DownstreamServiceException exception = assertThrows(DownstreamServiceException.class, () -> {
            fallbackClient.getAllPayouts("token");
        });

        String message = exception.getMessage();
        assertNotNull(message);
        assertTrue(message.contains("Payment") || message.contains("unavailable"));
    }

    @Test
    void testProcessPayoutRequestWithNullCause() {
        PaymentClient client = fallbackFactory.create(null);
        ProcessPayoutRequest request = new ProcessPayoutRequest();
        
        DownstreamServiceException exception = assertThrows(DownstreamServiceException.class, () -> {
            client.processPayoutRequest(request, "token");
        });
        
        assertNotNull(exception.getMessage());
    }

    @Test
    void testProcessPayoutRequestByPathWithNullCause() {
        PaymentClient client = fallbackFactory.create(null);
        
        DownstreamServiceException exception = assertThrows(DownstreamServiceException.class, () -> {
            client.processPayoutRequestByPath("token", "APPROVE", "id", "remarks");
        });
        
        assertNotNull(exception.getMessage());
    }

    @Test
    void testGetTransactionHistoryWithNullCause() {
        PaymentClient client = fallbackFactory.create(null);
        
        DownstreamServiceException exception = assertThrows(DownstreamServiceException.class, () -> {
            client.getTransactionHistory();
        });
        
        assertNotNull(exception.getMessage());
    }

    @Test
    void testExceptionIncludesCause() {
        RuntimeException cause = new RuntimeException("Original error");
        PaymentClient client = fallbackFactory.create(cause);
        
        DownstreamServiceException exception = assertThrows(DownstreamServiceException.class, () -> {
            client.getAllPayouts("token");
        });
        
        assertSame(cause, exception.getCause());
    }
}

