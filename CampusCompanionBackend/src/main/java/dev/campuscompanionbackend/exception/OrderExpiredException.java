package dev.campuscompanionbackend.exception;

public class OrderExpiredException extends BusinessException {
    private final ErrorCode errorCode = ErrorCode.ORDER_EXPIRED;

    public OrderExpiredException(String message) {
        super(message);
    }

    public OrderExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
