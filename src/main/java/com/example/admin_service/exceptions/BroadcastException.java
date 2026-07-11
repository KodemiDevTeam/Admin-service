package com.example.admin_service.exceptions;

public class BroadcastException extends RuntimeException {
    public BroadcastException(String message) {
        super(message);
    }

    public BroadcastException(String message, Throwable cause) {
        super(message, cause);
    }
}
