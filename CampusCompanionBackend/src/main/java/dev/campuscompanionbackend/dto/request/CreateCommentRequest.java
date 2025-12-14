package dev.campuscompanionbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发布评论请求参数
 */
@Data
public class CreateCommentRequest {
    
    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    private String content;
    
    /**
     * 父评论ID，为空则表示回复动态
     */
    private Long parentId;
}