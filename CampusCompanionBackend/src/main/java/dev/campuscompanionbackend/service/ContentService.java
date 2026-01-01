package dev.campuscompanionbackend.service;

import dev.campuscompanionbackend.dto.request.CreateCommentRequest;
import dev.campuscompanionbackend.dto.request.CreateContentRequest;
import dev.campuscompanionbackend.entity.PostMedia;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 动态服务接口
 */
public interface ContentService {
    
    /**
     * 发布动态
     * @param request 发布动态请求参数
     * @return Long 动态ID
     */
    Long createContent(CreateContentRequest request);
    
    /**
     * 获取动态列表
     * @param page 页码
     * @param size 每页数量
     * @param type 内容类型
     * @return Object 分页动态列表
     */
    Object getContents(Integer page, Integer size, Integer type);
    
    /**
     * 获取动态详情
     * @param contentId 动态ID
     * @return Object 动态详情
     */
    Object getContentDetail(Long contentId);

    /**
     * 修改动态内容
     * @param contentId 动态ID
     * @param request 修改动态请求参数
     * @return Long 动态ID
     */
    Long updateContent(Long contentId, CreateContentRequest request);
    
    /**
     * 删除动态
     * @param contentId 动态ID
     */
    void deleteContent(Long contentId);
    
    /**
     * 上传媒体文件
     * @param contentId 动态ID
     * @param media 媒体文件
     * @return String 媒体文件URL
     */
    String uploadMedia(Long contentId, MultipartFile media);

    /**
     * 获取动态所有媒体文件
     * @param contentId 动态ID
     * @return List<PostMedia> 所有媒体文件
     */
    List<PostMedia> getMedias(Long contentId);
    
    /**
     * 发布评论
     * @param contentId 动态ID
     * @param request 发布评论请求参数
     * @return Long 评论ID
     */
    Long createComment(Long contentId, CreateCommentRequest request);
    
    /**
     * 获取评论列表
     * @param contentId 动态ID
     * @param page 页码
     * @param size 每页数量
     * @return Object 分页评论列表
     */
    Object getComments(Long contentId, Integer page, Integer size);

    /**
     * 点赞/取消点赞
     * @param contentId 内容ID
     * @return Map<String, Object> 包含点赞状态和点赞数
     */
    Map<String, Object> likeContent(Long contentId);
    
    /**
     * 获取点赞列表
     * @param contentId 内容ID
     * @return Object 点赞用户列表
     */
    Object getLikes(Long contentId);

    /**
     * 根据关键字查询动态列表
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页数量
     * @param type 内容类型
     * @return Object 动态列表
     */
    Object searchByKeyword(String keyword, Integer page, Integer size, Integer type);
}