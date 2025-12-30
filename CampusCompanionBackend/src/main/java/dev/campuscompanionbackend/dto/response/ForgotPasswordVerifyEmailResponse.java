package dev.campuscompanionbackend.dto.response;

import lombok.Data;

/**
 * 忘记密码 - 验证邮箱响应数据
 */
@Data
public class ForgotPasswordVerifyEmailResponse {

    /**
     * 脱敏后的邮箱展示，例如 te***@example.com
     */
    private String emailMasked;
}
