package com.example.admin_service.exceptions;

public class UserSuspendException extends RuntimeException {
    public UserSuspendException(String message) {
        super(message);
    }

    public UserSuspendException(String message, Throwable cause) {
        super(message, cause);
    }
}
