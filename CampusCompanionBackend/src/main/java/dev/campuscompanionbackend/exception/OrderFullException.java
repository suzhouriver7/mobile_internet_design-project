package dev.campuscompanionbackend.exception;

public class OrderFullException extends BusinessException{
    private final ErrorCode errorCode = ErrorCode.ORDER_FULL;

    public OrderFullException(String message) {
        super(message);
    }

    public OrderFullException(String message, Throwable cause) {
        super(message, cause);
    }
}
