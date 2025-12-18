package dev.campuscompanionbackend.dto.request;

import dev.campuscompanionbackend.enums.MediaType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发布动态请求参数
 */
@Data
public class CreateContentRequest {
    
    /**
     * 动态文本内容
     */
    @NotBlank(message = "动态内容不能为空")
    private String content;
    
    /**
     * 媒体类型，默认TEXT
     */
    private MediaType mediaType;
    
    /**
     * 关联的订单ID
     */
    private Long orderId;
}