package dev.campuscompanionbackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 忘记密码 - 验证邮箱请求参数
 */
@Data
public class ForgotPasswordVerifyEmailRequest {

    /**
     * 注册邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
}
