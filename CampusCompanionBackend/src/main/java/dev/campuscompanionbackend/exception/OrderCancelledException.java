package dev.campuscompanionbackend.exception;

public class OrderCancelledException extends BusinessException {
    private final ErrorCode errorCode = ErrorCode.ORDER_CANCELLED;

    public OrderCancelledException(String message) {
        super(message);
    }

    public OrderCancelledException(String message, Throwable cause) {
        super(message, cause);
    }
}
