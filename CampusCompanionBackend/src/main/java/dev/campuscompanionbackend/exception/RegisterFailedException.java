package dev.campuscompanionbackend.exception;

public class RegisterFailedException extends BusinessException {
    private final ErrorCode errorCode = ErrorCode.REGISTER_FAILED;

    public RegisterFailedException(String message) {
        super(message);
    }

    public RegisterFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
