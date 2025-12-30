package dev.campuscompanionbackend.exception;

public class OrderCompletedException extends BusinessException {
    private final ErrorCode errorCode = ErrorCode.ORDER_COMPLETED;

    public OrderCompletedException(String message) {
        super(message);
    }

    public OrderCompletedException(String message, Throwable cause) {
        super(message, cause);
    }
}
