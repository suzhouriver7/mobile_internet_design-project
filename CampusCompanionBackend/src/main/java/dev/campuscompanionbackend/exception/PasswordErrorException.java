package dev.campuscompanionbackend.exception;

public class PasswordErrorException extends BusinessException {
    private final ErrorCode errorCode = ErrorCode.PASSWORD_ERROR;

    public PasswordErrorException(String message) {
        super(message);
    }

    public PasswordErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
