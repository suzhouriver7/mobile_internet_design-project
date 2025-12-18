package dev.campuscompanionbackend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 接受订单请求参数
 */
@Data
public class AcceptOrderRequest {
    
    /**
     * 被接受的用户ID
     */
    @NotNull(message = "被接受的用户ID不能为空")
    private Long accepterId;
}