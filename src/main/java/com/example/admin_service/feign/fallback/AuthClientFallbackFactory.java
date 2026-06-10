package com.example.admin_service.feign.fallback;

import com.example.admin_service.feign.AuthClient;
import com.example.admin_service.dto.response.AdminLoginRequest;
import com.example.admin_service.dto.request.UserDTO;
import com.example.admin_service.dto.request.TrainerReviewRequest;
import com.example.admin_service.exceptions.DownstreamServiceException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AuthClientFallbackFactory implements FallbackFactory<AuthClient> {
    private static final String UNKNOWN_ERROR = "Unknown error";

    @Override
    public AuthClient create(Throwable cause) {
        return new AuthClient() {
            @Override
            public ResponseEntity<Object> reviewTrainer(String token, String userId, TrainerReviewRequest request) {
                String errorMessage = cause != null ? cause.getMessage() : UNKNOWN_ERROR;
                log.error("AuthClient reviewTrainer failed: {}", errorMessage, cause);
                throw new DownstreamServiceException("Auth service is currently unavailable.", cause);
            }

            @Override
            public ResponseEntity<Object> checkStatus(String email) {
                String errorMessage = cause != null ? cause.getMessage() : UNKNOWN_ERROR;
                log.error("AuthClient checkStatus failed: {}", errorMessage, cause);
                throw new DownstreamServiceException("Auth service is currently unavailable.", cause);
            }

            @Override
            public Object adminLogin(AdminLoginRequest adminResponseDTO) {
                String errorMessage = cause != null ? cause.getMessage() : UNKNOWN_ERROR;
                log.error("AuthClient adminLogin failed: {}", errorMessage, cause);
                throw new DownstreamServiceException("Auth service is currently unavailable.", cause);
            }

            @Override
            public UserDTO geUserById(String token, String userId) {
                String errorMessage = cause != null ? cause.getMessage() : UNKNOWN_ERROR;
                log.error("AuthClient geUserById failed: {}", errorMessage, cause);
                throw new DownstreamServiceException("Auth service is currently unavailable.", cause);
            }
        };
    }
}
