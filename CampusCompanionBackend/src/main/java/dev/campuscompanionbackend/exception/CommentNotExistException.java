package dev.campuscompanionbackend.exception;

public class CommentNotExistException extends BusinessException {
    private final ErrorCode errorCode = ErrorCode.COMMENT_NOT_EXIST;

    public CommentNotExistException(String message) {
        super(message);
    }

    public CommentNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
