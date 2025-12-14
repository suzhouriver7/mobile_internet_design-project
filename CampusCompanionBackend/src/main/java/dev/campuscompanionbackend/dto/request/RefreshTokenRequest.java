package dev.campuscompanionbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新Token请求参数
 */
@Data
public class RefreshTokenRequest {
    
    /**
     * 刷新令牌
     */
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}