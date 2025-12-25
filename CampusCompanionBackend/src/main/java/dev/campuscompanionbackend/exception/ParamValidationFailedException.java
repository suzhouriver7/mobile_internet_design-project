package dev.campuscompanionbackend.exception;

public class ParamValidationFailedException extends BusinessException {
    private final ErrorCode errorCode = ErrorCode.PARAM_VALIDATION_FAILED;

    public ParamValidationFailedException(String message) {
        super(message);
    }

    public ParamValidationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
