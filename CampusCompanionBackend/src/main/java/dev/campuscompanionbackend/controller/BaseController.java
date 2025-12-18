package dev.campuscompanionbackend.controller;

import dev.campuscompanionbackend.dto.response.ApiResponse;
import org.springframework.stereotype.Controller;

/**
 * 基础Controller，所有Controller的父类
 */
@Controller
public class BaseController {
    
    /**
     * 成功响应
     * @param data 响应数据
     * @return ApiResponse
     * @param <T> 响应数据类型
     */
    protected <T> ApiResponse<T> success(T data) {
        return ApiResponse.success(data);
    }
    
    /**
     * 成功响应，带自定义消息
     * @param message 响应消息
     * @param data 响应数据
     * @return ApiResponse
     * @param <T> 响应数据类型
     */
    protected <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }
    
    /**
     * 失败响应
     * @param code 错误码
     * @param message 错误消息
     * @return ApiResponse
     * @param <T> 响应数据类型
     */
    protected <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.error(code, message);
    }
    
    /**
     * 失败响应，使用默认错误码
     * @param message 错误消息
     * @return ApiResponse
     * @param <T> 响应数据类型
     */
    protected <T> ApiResponse<T> error(String message) {
        return ApiResponse.error(message);
    }
}