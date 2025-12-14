package dev.campuscompanionbackend.controller;

import dev.campuscompanionbackend.dto.request.ChangePasswordRequest;
import dev.campuscompanionbackend.dto.request.UpdateUserRequest;
import dev.campuscompanionbackend.dto.response.ApiResponse;
import dev.campuscompanionbackend.dto.response.UserInfoResponse;
import dev.campuscompanionbackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户管理相关接口
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController extends BaseController {
    
    private final UserService userService;
    
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * 获取用户详情
     * @param userId 用户ID
     * @return ApiResponse<UserInfoResponse>
     */
    @GetMapping("/{userId}")
    public ApiResponse<UserInfoResponse> getUserInfo(@PathVariable Long userId) {
        UserInfoResponse userInfo = userService.getUserInfo(userId);
        return success(userInfo);
    }
    
    /**
     * 更新用户信息
     * @param userId 用户ID
     * @param request 更新用户信息请求参数
     * @return ApiResponse<UserInfoResponse>
     */
    @PutMapping("/{userId}")
    public ApiResponse<UserInfoResponse> updateUserInfo(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request) {
        UserInfoResponse userInfo = userService.updateUserInfo(userId, request);
        return success("更新成功", userInfo);
    }
    
    /**
     * 修改密码
     * @param userId 用户ID
     * @param request 修改密码请求参数
     * @return ApiResponse<Void>
     */
    @PutMapping("/{userId}/password")
    public ApiResponse<Void> changePassword(
            @PathVariable Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userId, request);
        return success("密码修改成功", null);
    }
    
    /**
     * 上传头像
     * @param userId 用户ID
     * @param avatar 头像文件
     * @return ApiResponse<String>
     */
    @PostMapping("/{userId}/avatar")
    public ApiResponse<String> uploadAvatar(
            @PathVariable Long userId,
            @RequestParam("avatar") MultipartFile avatar) {
        String avatarUrl = userService.uploadAvatar(userId, avatar);
        return success("上传成功", avatarUrl);
    }
    
    /**
     * 搜索用户
     * @param keyword 搜索关键词
     * @param page 页码，默认1
     * @param size 每页数量，默认10
     * @return ApiResponse<Object>
     */
    @GetMapping("/search")
    public ApiResponse<Object> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Object result = userService.searchUsers(keyword, page, size);
        return success(result);
    }
}