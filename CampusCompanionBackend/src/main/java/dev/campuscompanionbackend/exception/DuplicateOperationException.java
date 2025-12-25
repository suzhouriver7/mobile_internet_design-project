package dev.campuscompanionbackend.exception;

public class DuplicateOperationException extends BusinessException {
    private final ErrorCode errorCode = ErrorCode.DUPLICATE_OPERATION;

    public DuplicateOperationException(String message) {
        super(message);
    }

    public DuplicateOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
