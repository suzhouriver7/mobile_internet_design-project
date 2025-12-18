package dev.campuscompanionbackend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户信息请求参数
 */
@Data
public class UpdateUserRequest {
    
    /**
     * 昵称
     */
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;
    
    /**
     * 头像URL
     */
    @Size(max = 255, message = "头像URL长度不能超过255个字符")
    private String avatarUrl;
    
    /**
     * 个性签名
     */
    @Size(max = 255, message = "个性签名长度不能超过255个字符")
    private String signature;
}