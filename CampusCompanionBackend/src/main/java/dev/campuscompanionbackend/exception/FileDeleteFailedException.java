package dev.campuscompanionbackend.exception;

public class FileDeleteFailedException extends BusinessException {
    public FileDeleteFailedException(String message) {
        super(message);
    }

    public FileDeleteFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
