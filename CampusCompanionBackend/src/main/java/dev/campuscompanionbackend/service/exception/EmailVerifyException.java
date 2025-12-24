package dev.campuscompanionbackend.service.exception;

public class EmailVerifyException extends RuntimeException {
    public EmailVerifyException(String message, Throwable cause) {
        super(message, cause);
    }
}
