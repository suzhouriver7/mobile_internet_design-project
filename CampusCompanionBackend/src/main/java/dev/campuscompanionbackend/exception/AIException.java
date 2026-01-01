package dev.campuscompanionbackend.exception;

public class AIException extends BusinessException{
    private ErrorCode errorCode = ErrorCode.AI_FAILED;

    public AIException(String message) {
        super(message);
    }

    public AIException(String message, Throwable cause) {
        super(message, cause);
    }

    public AIException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AIException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
