package dev.campuscompanionbackend.dto.request;

import dev.campuscompanionbackend.enums.ActivityType;
import dev.campuscompanionbackend.enums.Campus;
import dev.campuscompanionbackend.enums.GenderRequire;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 创建订单请求参数
 */
@Data
public class CreateOrderRequest {
    
    /**
     * 活动类型
     */
    @NotNull(message = "活动类型不能为空")
    private ActivityType activityType;
    
    /**
     * 性别要求
     */
    @NotNull(message = "性别要求不能为空")
    private GenderRequire genderRequire;
    
    /**
     * 校区
     */
    @NotNull(message = "校区不能为空")
    private Campus campus;
    
    /**
     * 活动地点
     */
    @NotBlank(message = "活动地点不能为空")
    private String location;
    
    /**
     * 活动开始时间，格式：yyyy-MM-dd HH:mm:ss
     */
    @NotBlank(message = "活动开始时间不能为空")
    private String startTime;
    
    /**
     * 备注
     */
    private String note;
    
    /**
     * 需要的人数上限
     */
    @Positive(message = "人数上限必须为正数")
    @NotNull(message = "人数上限不能为空")
    private Integer maxPeople;
}