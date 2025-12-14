package dev.campuscompanionbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应格式
 * @param <T> 响应数据类型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    
    /**
     * 响应状态码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 响应时间戳
     */
    private Long timestamp;
    
    /**
     * 成功响应
     * @param data 响应数据
     * @return ApiResponse
     * @param <T> 响应数据类型
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage("成功");
        response.setData(data);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
    
    /**
     * 成功响应，带自定义消息
     * @param message 响应消息
     * @param data 响应数据
     * @return ApiResponse
     * @param <T> 响应数据类型
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
    
    /**
     * 失败响应
     * @param code 错误码
     * @param message 错误消息
     * @return ApiResponse
     * @param <T> 响应数据类型
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        response.setData(null);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
    
    /**
     * 失败响应，使用默认错误码
     * @param message 错误消息
     * @return ApiResponse
     * @param <T> 响应数据类型
     */
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(400);
        response.setMessage(message);
        response.setData(null);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
}