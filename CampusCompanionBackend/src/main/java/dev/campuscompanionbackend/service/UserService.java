package dev.campuscompanionbackend.service;

import dev.campuscompanionbackend.dto.request.ChangePasswordRequest;
import dev.campuscompanionbackend.dto.request.UpdateUserRequest;
import dev.campuscompanionbackend.dto.response.UserInfoResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 获取用户详情
     * @param userId 用户ID
     * @return UserInfoResponse 用户信息
     */
    UserInfoResponse getUserInfo(Long userId);
    
    /**
     * 更新用户信息
     * @param userId 用户ID
     * @param request 更新用户信息请求参数
     * @return UserInfoResponse 更新后的用户信息
     */
    UserInfoResponse updateUserInfo(Long userId, UpdateUserRequest request);
    
    /**
     * 修改密码
     * @param userId 用户ID
     * @param request 修改密码请求参数
     */
    void changePassword(Long userId, ChangePasswordRequest request);
    
    /**
     * 上传头像
     * @param userId 用户ID
     * @param avatar 头像文件
     * @return String 头像URL
     */
    String uploadAvatar(Long userId, MultipartFile avatar);
    
    /**
     * 搜索用户
     * @param keyword 搜索关键词（昵称或学号）
     * @param page 页码
     * @param size 每页数量
     * @return Object 分页用户列表
     */
    Object searchUsers(String keyword, Integer page, Integer size);

    void resetPassword();
}