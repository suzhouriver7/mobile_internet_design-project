package dev.campuscompanionbackend.exception;

public class ApplicationNotExistException extends BusinessException {
    private final ErrorCode errorCode = ErrorCode.APPLICATION_NOT_EXIST;

    public ApplicationNotExistException(String message) {
        super(message);
    }

    public ApplicationNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
