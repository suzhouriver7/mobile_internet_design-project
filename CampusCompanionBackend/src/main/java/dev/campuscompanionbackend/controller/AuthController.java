package dev.campuscompanionbackend.controller;

import dev.campuscompanionbackend.dto.request.LoginRequest;
import dev.campuscompanionbackend.dto.request.RefreshTokenRequest;
import dev.campuscompanionbackend.dto.request.RegisterRequest;
import dev.campuscompanionbackend.dto.response.ApiResponse;
import dev.campuscompanionbackend.dto.response.LoginResponse;
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
}