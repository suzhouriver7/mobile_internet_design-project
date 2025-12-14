package dev.campuscompanionbackend.dto.response;

import lombok.Data;

/**
 * 用户信息响应DTO
 */
@Data
public class UserInfoResponse {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 头像URL
     */
    private String avatarUrl;
    
    /**
     * 用户类型
     */
    private Integer userType;
}
