package dev.campuscompanionbackend.exception;

public class FileUploadFailedException extends BusinessException {
    private final ErrorCode errorCode = ErrorCode.FILE_UPLOAD_FAILED;

    public FileUploadFailedException(String message) {
        super(message);
    }

    public FileUploadFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
