package dev.campuscompanionbackend.service;

import dev.campuscompanionbackend.dto.request.LoginRequest;
import dev.campuscompanionbackend.dto.request.RegisterRequest;
import dev.campuscompanionbackend.dto.request.ForgotPasswordVerifyEmailRequest;
import dev.campuscompanionbackend.dto.request.ForgotPasswordVerifyCodeRequest;
import dev.campuscompanionbackend.dto.request.ForgotPasswordResetPasswordRequest;
import dev.campuscompanionbackend.dto.response.LoginResponse;
import dev.campuscompanionbackend.dto.response.ForgotPasswordVerifyEmailResponse;


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
     * @param userId 退出的用户ID
     */
    void logout(Long userId);

    // ==================== 忘记密码相关接口（仅定义签名，具体逻辑待实现） ====================

    /**
     * 忘记密码 - 验证邮箱是否已注册
     * 对应接口：POST /auth/forgot/verify-email
     * @param request 验证邮箱请求参数
     * @return ForgotPasswordVerifyEmailResponse，包含脱敏后的邮箱展示
     */
    ForgotPasswordVerifyEmailResponse verifyEmailForReset(ForgotPasswordVerifyEmailRequest request);

    /**
     * 忘记密码 - 向邮箱发送重置验证码
     * 对应接口：POST /auth/forgot/send-code
     * @param request 请求参数，包含注册邮箱
     */
    void sendResetCode(ForgotPasswordVerifyEmailRequest request);

    /**
     * 忘记密码 - 校验验证码
     * 对应接口：POST /auth/forgot/verify-code
     * @param request 请求参数，包含邮箱和验证码
     */
    void verifyResetCode(ForgotPasswordVerifyCodeRequest request);

    /**
     * 忘记密码 - 重置密码
     * 对应接口：POST /auth/forgot/reset-password
     * @param request 请求参数，包含邮箱、验证码和新密码
     */
    void resetPassword(ForgotPasswordResetPasswordRequest request);
}