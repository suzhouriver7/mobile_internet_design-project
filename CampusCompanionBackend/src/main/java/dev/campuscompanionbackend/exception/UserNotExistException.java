package dev.campuscompanionbackend.exception;

public class UserNotExistException extends BusinessException {
    public final ErrorCode errorCode = ErrorCode.USER_NOT_EXIST;

    public UserNotExistException(String message) {
        super(message);
    }

    public UserNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
