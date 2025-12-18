package dev.campuscompanionbackend.exception;

public enum ErrorCode {
    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    PARAM_VALIDATION_FAILED(1001, "参数校验失败"),
    USER_NOT_EXIST(1002, "用户不存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    NO_PERMISSION(1004, "无权限操作"),
    ORDER_NOT_EXIST(1005, "订单不存在"),
    CONTENT_NOT_EXIST(1006, "动态不存在"),
    FILE_UPLOAD_FAILED(1007, "文件上传失败"),
    DUPLICATE_OPERATION(1008, "重复操作"),
    ORDER_FULL(1009, "人数已满"),
    ORDER_EXPIRED(1010, "订单已过期"),
    USER_EXISTS(1011, "用户已存在"),
    TOKEN_INVALID(1012, "令牌无效"),
    APPLICATION_NOT_EXIST(1013, "申请记录不存在"),
    ORDER_COMPLETED(1014, "订单已完成"),
    ORDER_CANCELLED(1015, "订单已取消"),
    COMMENT_NOT_EXIST(1016, "评论不存在");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}