package dev.campuscompanionbackend.service;

import dev.campuscompanionbackend.dto.request.LoginRequest;
import dev.campuscompanionbackend.dto.request.RegisterRequest;
import dev.campuscompanionbackend.dto.response.LoginResponse;

/**
 * 用户认证服务接口
 */
public interface AuthService {
    
    /**
     * 用户登录
     * @param request 登录请求参数
     * @return LoginResponse 登录响应，包含token和用户信息
     */
    LoginResponse login(LoginRequest request);
    
    /**
     * 用户注册
     * @param request 注册请求参数
     * @return Long 注册成功的用户ID
     */
    Long register(RegisterRequest request);
    
    /**
     * 刷新Token
     * @param refreshToken 刷新Token
     * @return String 新的访问Token
     */
    String refreshToken(String refreshToken);
    
    /**
     * 用户退出登录
     */
    void logout();
}