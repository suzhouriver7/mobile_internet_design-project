package dev.campuscompanionbackend.exception;

public class OrderNotExistException extends BusinessException {
    private final ErrorCode errorCode = ErrorCode.ORDER_NOT_EXIST;

    public OrderNotExistException(String message) {
        super(message);
    }

    public OrderNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
