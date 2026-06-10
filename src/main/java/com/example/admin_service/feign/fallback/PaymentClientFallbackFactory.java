package com.example.admin_service.feign.fallback;

import com.example.admin_service.feign.PaymentClient;
import com.example.admin_service.dto.request.PayoutRequest;
import com.example.admin_service.dto.request.ProcessPayoutRequest;
import com.example.admin_service.dto.response.TransactionHistoryResponse;
import com.example.admin_service.exceptions.DownstreamServiceException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
@Component
public class PaymentClientFallbackFactory implements FallbackFactory<PaymentClient> {
    private static final String UNKNOWN_ERROR = "Unknown error";

    @Override
    public PaymentClient create(Throwable cause) {
        return new PaymentClientFallback(cause);
    }

    private static class PaymentClientFallback implements PaymentClient {
        private final Throwable cause;

        PaymentClientFallback(Throwable cause) {
            this.cause = cause;
        }

        @Override
        public List<PayoutRequest> getAllPayouts(String token) {
            String errorMessage = cause != null ? cause.getMessage() : UNKNOWN_ERROR;
            log.error("PaymentClient getAllPayouts failed: {}", errorMessage, cause);
            throw new DownstreamServiceException("Payment service is currently unavailable.", cause);
        }

        @Override
        public String processPayoutRequest(ProcessPayoutRequest request, String token) {
            String errorMessage = cause != null ? cause.getMessage() : UNKNOWN_ERROR;
            log.error("PaymentClient processPayoutRequest failed: {}", errorMessage, cause);
            throw new DownstreamServiceException("Payment service is currently unavailable.", cause);
        }

        @Override
        public String processPayoutRequestByPath(String token, String action, String payoutId, String remarks) {
            String errorMessage = cause != null ? cause.getMessage() : UNKNOWN_ERROR;
            log.error("PaymentClient processPayoutRequestByPath failed: {}", errorMessage, cause);
            throw new DownstreamServiceException("Payment service is currently unavailable.", cause);
        }

        @Override
        public TransactionHistoryResponse getTransactionHistory() {
            String errorMessage = cause != null ? cause.getMessage() : UNKNOWN_ERROR;
            log.error("PaymentClient getTransactionHistory failed: {}", errorMessage, cause);
            throw new DownstreamServiceException("Payment service is currently unavailable.", cause);
        }
    }
}
