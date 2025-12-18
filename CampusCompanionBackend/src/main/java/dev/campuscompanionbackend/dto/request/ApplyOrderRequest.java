package dev.campuscompanionbackend.dto.request;

import lombok.Data;

/**
 * 申请加入订单请求参数
 */
@Data
public class ApplyOrderRequest {
    
    /**
     * 申请留言
     */
    private String message;
}