package dev.campuscompanionbackend.controller;

import dev.campuscompanionbackend.dto.request.CreateCommentRequest;
import dev.campuscompanionbackend.dto.request.CreateContentRequest;
import dev.campuscompanionbackend.dto.response.ApiResponse;
import dev.campuscompanionbackend.service.ContentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 动态管理相关接口
 */
@RestController
@RequestMapping("/api/v1/contents")
public class ContentController extends BaseController {
    
    private final ContentService contentService;
    
    @Autowired
    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }
    
    /**
     * 发布动态
     * @param request 发布动态请求参数
     * @return ApiResponse<Long>
     */
    @PostMapping
    public ApiResponse<Long> createContent(@Valid @RequestBody CreateContentRequest request) {
        Long contentId = contentService.createContent(request);
        return success("发布成功", contentId);
    }
    
    /**
     * 获取动态列表
     * @param page 页码，默认1
     * @param size 每页数量，默认10
     * @param type 内容类型
     * @return ApiResponse<Object>
     */
    @GetMapping
    public ApiResponse<Object> getContents(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer type) {
        Object result = contentService.getContents(page, size, type);
        return success(result);
    }
    
    /**
     * 获取动态详情
     * @param contentId 动态ID
     * @return ApiResponse<Object>
     */
    @GetMapping("/{contentId}")
    public ApiResponse<Object> getContentDetail(@PathVariable Long contentId) {
        Object detail = contentService.getContentDetail(contentId);
        return success(detail);
    }
    
    /**
     * 删除动态
     * @param contentId 动态ID
     * @return ApiResponse<Void>
     */
    @DeleteMapping("/{contentId}")
    public ApiResponse<Void> deleteContent(@PathVariable Long contentId) {
        contentService.deleteContent(contentId);
        return success("删除成功", null);
    }
    
    /**
     * 上传媒体文件
     * @param contentId 动态ID
     * @param media 媒体文件
     * @return ApiResponse<String>
     */
    @PostMapping("/{contentId}/media")
    public ApiResponse<String> uploadMedia(
            @PathVariable Long contentId,
            @RequestParam("media") MultipartFile media) {
        String mediaUrl = contentService.uploadMedia(contentId, media);
        return success("上传成功", mediaUrl);
    }
    
    /**
     * 发布评论
     * @param contentId 动态ID
     * @param request 发布评论请求参数
     * @return ApiResponse<Long>
     */
    @PostMapping("/{contentId}/comments")
    public ApiResponse<Long> createComment(
            @PathVariable Long contentId,
            @Valid @RequestBody CreateCommentRequest request) {
        Long commentId = contentService.createComment(contentId, request);
        return success("评论成功", commentId);
    }
    
    /**
     * 获取评论列表
     * @param contentId 动态ID
     * @param page 页码，默认1
     * @param size 每页数量，默认20
     * @return ApiResponse<Object>
     */
    @GetMapping("/{contentId}/comments")
    public ApiResponse<Object> getComments(
            @PathVariable Long contentId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Object result = contentService.getComments(contentId, page, size);
        return success(result);
    }
    
    /**
     * 点赞/取消点赞
     * @param contentId 内容ID
     * @return ApiResponse<Object>
     */
    @PostMapping("/{contentId}/like")
    public ApiResponse<Object> likeContent(@PathVariable Long contentId) {
        Object result = contentService.likeContent(contentId);
        return success("点赞成功", result);
    }
    
    /**
     * 获取点赞列表
     * @param contentId 内容ID
     * @return ApiResponse<Object>
     */
    @GetMapping("/{contentId}/likes")
    public ApiResponse<Object> getLikes(@PathVariable Long contentId) {
        Object result = contentService.getLikes(contentId);
        return success(result);
    }
}