package com.example.admin_service.exceptions;

public class ValueNotFoundException extends RuntimeException {
    public ValueNotFoundException(String message) {
        super(message);
    }
}
