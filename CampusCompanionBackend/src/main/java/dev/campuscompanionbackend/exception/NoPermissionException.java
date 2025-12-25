package dev.campuscompanionbackend.exception;

public class NoPermissionException extends BusinessException {
    private final ErrorCode errorCode = ErrorCode.NO_PERMISSION;

    public NoPermissionException(String message) {
        super(message);
    }

    public NoPermissionException(String message, Throwable cause) {
        super(message, cause);
    }
}
