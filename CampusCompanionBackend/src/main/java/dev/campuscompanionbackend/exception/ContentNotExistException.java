package dev.campuscompanionbackend.exception;

public class ContentNotExistException extends BusinessException {
    private final ErrorCode errorCode = ErrorCode.CONTENT_NOT_EXIST;

    public ContentNotExistException(String message) {
        super(message);
    }

    public ContentNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
