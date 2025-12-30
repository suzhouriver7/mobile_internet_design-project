package dev.campuscompanionbackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 忘记密码 - 重置密码请求参数
 */
@Data
public class ForgotPasswordResetPasswordRequest {

    /**
     * 注册邮箱
     */
    @NotNull(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 6位数字验证码
     */
    @NotNull(message = "验证码不能为空")
    private Integer verifyCode;

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度必须在6-20个字符之间")
    private String newPassword;
}
