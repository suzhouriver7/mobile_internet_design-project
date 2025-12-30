package dev.campuscompanionbackend.controller;

import dev.campuscompanionbackend.dto.request.LoginRequest;
import dev.campuscompanionbackend.dto.request.RefreshTokenRequest;
import dev.campuscompanionbackend.dto.request.RegisterRequest;
import dev.campuscompanionbackend.dto.request.ForgotPasswordVerifyEmailRequest;
import dev.campuscompanionbackend.dto.request.ForgotPasswordVerifyCodeRequest;
import dev.campuscompanionbackend.dto.request.ForgotPasswordResetPasswordRequest;
import dev.campuscompanionbackend.dto.response.ApiResponse;
import dev.campuscompanionbackend.dto.response.LoginResponse;
import dev.campuscompanionbackend.dto.response.ForgotPasswordVerifyEmailResponse;
import dev.campuscompanionbackend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户认证相关接口
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController extends BaseController {
    
    private final AuthService authService;
    
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * 用户登录
     * @param request 登录请求参数
     * @return ApiResponse<LoginResponse>
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return success("登录成功", response);
    }
    
    /**
     * 用户注册
     * @param request 注册请求参数
     * @return ApiResponse<Long>
     */
    @PostMapping("/register")
    public ApiResponse<Long> register(@Valid @RequestBody RegisterRequest request) {
        Long userId = authService.register(request);
        return success("注册成功", userId);
    }
    
    /**
     * 刷新Token
     * @param request 刷新Token请求参数
     * @return ApiResponse<String>
     */
    @PostMapping("/refresh")
    public ApiResponse<String> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        String newToken = authService.refreshToken(request.getRefreshToken());
        return success("刷新成功", newToken);
    }
    
    /**
     * 用户退出登录
     * 当前实现通过请求头 X-User-Id 识别退出的用户，并将其在线状态标记为 OFFLINE。
     * @param userId 当前退出用户的ID（来自请求头 X-User-Id）
     * @return ApiResponse<Void>
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        authService.logout(userId);
        return success("退出成功", null);
    }

    // ==================== 忘记密码相关接口（与前端 / 文档对齐，逻辑待实现） ====================

    /**
     * 忘记密码 - 验证邮箱是否已注册
     * 对应文档：1.5 忘记密码 - 验证邮箱
     * 请求路径：POST /api/v1/auth/forgot/verify-email
     */
    @PostMapping("/forgot/verify-email")
    public ApiResponse<ForgotPasswordVerifyEmailResponse> verifyEmailForReset(
            @Valid @RequestBody ForgotPasswordVerifyEmailRequest request) {
        ForgotPasswordVerifyEmailResponse response = authService.verifyEmailForReset(request);
        // TODO: 根据业务需要完善 message 文案
        return success("邮箱可用于找回密码", response);
    }

    /**
     * 忘记密码 - 发送重置验证码
     * 对应文档：1.6 忘记密码 - 发送验证码
     * 请求路径：POST /api/v1/auth/forgot/send-code
     */
    @PostMapping("/forgot/send-code")
    public ApiResponse<Void> sendResetCode(@Valid @RequestBody ForgotPasswordVerifyEmailRequest request) {
        authService.sendResetCode(request);
        // TODO: 根据业务需要完善 message 文案
        return success("验证码已发送", null);
    }

    /**
     * 忘记密码 - 校验验证码
     * 对应文档：1.7 忘记密码 - 校验验证码
     * 请求路径：POST /api/v1/auth/forgot/verify-code
     */
    @PostMapping("/forgot/verify-code")
    public ApiResponse<Void> verifyResetCode(@Valid @RequestBody ForgotPasswordVerifyCodeRequest request) {
        authService.verifyResetCode(request);
        // TODO: 根据业务需要完善 message 文案
        return success("验证码验证通过", null);
    }

    /**
     * 忘记密码 - 重置密码
     * 对应文档：1.8 忘记密码 - 重置密码
     * 请求路径：POST /api/v1/auth/forgot/reset-password
     */
    @PostMapping("/forgot/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ForgotPasswordResetPasswordRequest request) {
        authService.resetPassword(request);
        // TODO: 根据业务需要完善 message 文案
        return success("密码重置成功", null);
    }
}