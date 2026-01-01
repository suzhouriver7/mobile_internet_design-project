package dev.campuscompanionbackend.exception;

public class FileDeleteFailedException extends BusinessException {
    private final ErrorCode errorCode = ErrorCode.FILE_DELETE_FAILED;

    public FileDeleteFailedException(String message) {
        super(message);
    }

    public FileDeleteFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
