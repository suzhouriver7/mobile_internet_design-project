package dev.campuscompanionbackend.exception;

public class UserExistException extends BusinessException {
    private final ErrorCode errorCode = ErrorCode.USER_EXISTS;

    public UserExistException(String message) {
        super(message);
    }

    public UserExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
