package dev.campuscompanionbackend.exception;

public class EmailInvalidException extends BusinessException {
    private final ErrorCode errorCode = ErrorCode.EMAIL_INVALID;

    public EmailInvalidException(String message) {
        super(message);
    }

    public EmailInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}
