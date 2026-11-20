package com.spring.learn.boot.goalvault.exception;

// LEARNING: A domain-specific exception for state conflicts (e.g., duplicate resource, illegal state
// transition). Mapped to HTTP 409 in GlobalExceptionHandler.
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
