package dev.campuscompanionbackend.exception;

public abstract class BusinessException extends RuntimeException {
    private final ErrorCode errorCode = ErrorCode.BAD_REQUEST;

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(String message) {
        super(message);
    }

    public int getCode(){
        return errorCode.getCode();
    }

    public String getCodeType(){
        return errorCode.getMessage();
    }
}