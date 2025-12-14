package dev.campuscompanionbackend.dto.request;

import dev.campuscompanionbackend.enums.ApplyStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审核申请请求参数
 */
@Data
public class AuditApplyRequest {
    
    /**
     * 审核状态
     */
    @NotNull(message = "审核状态不能为空")
    private ApplyStatus status;
}