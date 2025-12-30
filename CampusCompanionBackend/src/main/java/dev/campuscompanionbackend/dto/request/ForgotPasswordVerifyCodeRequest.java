package dev.campuscompanionbackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 忘记密码 - 验证码校验请求参数
 */
@Data
public class ForgotPasswordVerifyCodeRequest {

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
    @Min(value = 100000, message = "验证码格式不正确")
    @Max(value = 999999, message = "验证码格式不正确")
    private Integer verifyCode;
}
