package dev.campuscompanionbackend.exception;

public class TokenInvalidException extends BusinessException {
    private final ErrorCode errorCode = ErrorCode.TOKEN_INVALID;

    public TokenInvalidException(String message) {
        super(message);
    }

    public TokenInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}
