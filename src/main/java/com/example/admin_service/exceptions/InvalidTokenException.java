package com.example.admin_service.exceptions;

import io.jsonwebtoken.ExpiredJwtException;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message, ExpiredJwtException expiredException) {
        super(message, expiredException);
    }
}
