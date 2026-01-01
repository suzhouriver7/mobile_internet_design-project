package dev.campuscompanionbackend.exception;

import dev.campuscompanionbackend.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * 全局异常处理
 */
@RestControllerAdvice
@lombok.extern.slf4j.Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        StringBuilder errorMessage = new StringBuilder();

        for (FieldError fieldError : fieldErrors) {
            errorMessage.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append("; ");
        }

        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.PARAM_VALIDATION_FAILED.getCode(),
                errorMessage.toString()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.info(e.getMessage());
        log.debug("业务异常", e);
        String msg = e.getCodeType() + ':' + e.getMessage().split(":")[0];
        ApiResponse<Void> response = ApiResponse.error(e.getCode(), msg);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理其他所有异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.warn("意料之外的异常!");
        log.debug("意料之外的异常！", e);
        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.INTERNAL_ERROR.getCode(),
                ErrorCode.INTERNAL_ERROR.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(SomethingHappenedException.class)
    public ResponseEntity<ApiResponse<Void>> handleException(SomethingHappenedException e) {
        log.warn("不愿看到的异常!");
        log.debug("不愿看到的异常！", e);
        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.SOMETHING_HAPPENED.getCode(),
                ErrorCode.SOMETHING_HAPPENED.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NoPermissionException.class)
    public ResponseEntity<ApiResponse<Void>> handleException(NoPermissionException e) {
        log.warn("接收到不允许的操作请求!");
        log.debug("不允许的操作请求！", e);
        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.SOMETHING_HAPPENED.getCode(),
                ErrorCode.SOMETHING_HAPPENED.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}