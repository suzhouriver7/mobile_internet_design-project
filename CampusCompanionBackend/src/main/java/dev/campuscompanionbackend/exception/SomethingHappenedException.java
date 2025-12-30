package dev.campuscompanionbackend.exception;

public class SomethingHappenedException extends BusinessException {
    private final ErrorCode errorCode = ErrorCode.SOMETHING_HAPPENED;

    public SomethingHappenedException(String message) {
        super(message);
    }

    public SomethingHappenedException(String message, Throwable cause) {
        super(message, cause);
    }
}
