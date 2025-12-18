package dev.campuscompanionbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发送订单消息请求参数
 */
@Data
public class SendOrderMessageRequest {
    
    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;
}