package dev.campuscompanionbackend.exception;

public class VerifyCodeErrorException extends BusinessException {
    private final ErrorCode errorCode = ErrorCode.VERIFY_CODE_ERROR;

    public VerifyCodeErrorException(String message) {
        super(message);
    }

    public VerifyCodeErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
