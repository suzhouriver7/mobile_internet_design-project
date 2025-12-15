package dev.campuscompanionbackend.service.impl;

import dev.campuscompanionbackend.dto.request.CreateCommentRequest;
import dev.campuscompanionbackend.dto.request.CreateContentRequest;
import dev.campuscompanionbackend.dto.response.PageResponse;
import dev.campuscompanionbackend.entity.*;
import dev.campuscompanionbackend.enums.ContentStatus;
import dev.campuscompanionbackend.enums.MediaType;
import dev.campuscompanionbackend.enums.PostType;
import dev.campuscompanionbackend.exception.BusinessException;
import dev.campuscompanionbackend.repository.*;
import dev.campuscompanionbackend.service.ContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public Long createContent(CreateContentRequest request) {
        log.info("发布动态: content={}, mediaType={}, orderId={}",
                request.getContent().substring(0, Math.min(50, request.getContent().length())),
                request.getMediaType(), request.getOrderId());

        Long currentUserId = 1L;
        User currentUser = getUserById(currentUserId);

        Post post = new Post();
        post.setUser(currentUser);
        post.setType(PostType.POST);
        post.setContent(request.getContent());
        post.setHasMedia(request.getMediaType() != null ? request.getMediaType() : MediaType.TEXT_ONLY);
        post.setStatus(ContentStatus.NORMAL);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        if (request.getOrderId() != null) {
            Order order = getOrderById(request.getOrderId());
            post.setOrder(order);
        }

        Post savedPost = postRepository.save(post);
        return savedPost.getPid();
    }

    @Override
    public Object getContents(Integer page, Integer size, Integer type) {
        log.info("获取动态列表: page={}, size={}, type={}", page, size, type);

        List<Post> posts;
        if (type != null && type == 1) {
            posts = postRepository.findByTypeAndStatusOrderByCreatedAtDesc(PostType.COMMENT, ContentStatus.NORMAL);
        } else {
            posts = postRepository.findByTypeAndStatusOrderByCreatedAtDesc(PostType.POST, ContentStatus.NORMAL);
        }

        int start = (page - 1) * size;
        int end = Math.min(start + size, posts.size());

        List<Post> pagedPosts = new ArrayList<>();
        for (int i = start; i < end && i < posts.size(); i++) {
            pagedPosts.add(posts.get(i));
        }

        List<Map<String, Object>> contentList = pagedPosts.stream()
                .map(this::convertToContentVO)
                .collect(Collectors.toList());

        return new PageResponse<>(
                contentList,
                (long) posts.size(),
                page,
                size
        );
    }

    @Override
    public Object getContentDetail(Long contentId) {
        log.info("获取动态详情: contentId={}", contentId);

        Post post = getPostById(contentId);
        Map<String, Object> contentDetail = convertToContentDetailVO(post);

        List<PostLike> likes = postLikeRepository.findByPost(post);
        contentDetail.put("likeCount", likes.size());

        List<Post> comments = postRepository.findByParentPostAndTypeAndStatusOrderByCreatedAtDesc(
                post, PostType.COMMENT, ContentStatus.NORMAL);
        contentDetail.put("commentCount", comments.size());

        return contentDetail;
    }

    @Override
    @Transactional
    public void deleteContent(Long contentId) {
        log.info("删除动态: contentId={}", contentId);

        Post post = getPostById(contentId);

        Long currentUserId = 1L;
        if (!post.getUser().getUid().equals(currentUserId)) {
            throw new BusinessException(1004, "只有动态发布者可以删除动态");
        }

        post.setStatus(ContentStatus.DELETED);
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);
    }

    @Override
    @Transactional
    public String uploadMedia(Long contentId, MultipartFile media) {
        log.info("上传媒体文件: contentId={}, filename={}", contentId, media.getOriginalFilename());

        Post post = getPostById(contentId);

        String contentType = media.getContentType();
        if (contentType == null) {
            throw new BusinessException(1007, "无法识别的文件类型");
        }

        String uploadDir;
        MediaType mediaType;

        if (contentType.startsWith("image/")) {
            mediaType = MediaType.IMAGE;
            uploadDir = "uploads/images/";
        } else if (contentType.startsWith("video/")) {
            mediaType = MediaType.VIDEO;
            uploadDir = "uploads/videos/";
        } else {
            throw new BusinessException(1007, "只支持图片和视频文件");
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = media.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = "media_" + contentId + "_" + System.currentTimeMillis() + fileExtension;

            Path filePath = uploadPath.resolve(filename);
            Files.copy(media.getInputStream(), filePath);

            String mediaUrl = "/" + uploadDir + filename;

            post.setHasMedia(mediaType);
            post.setUpdatedAt(LocalDateTime.now());
            postRepository.save(post);

            return mediaUrl;
        } catch (IOException e) {
            log.error("上传媒体文件失败", e);
            throw new BusinessException(1007, "上传媒体文件失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Long createComment(Long contentId, CreateCommentRequest request) {
        log.info("发布评论: contentId={}, parentId={}", contentId, request.getParentId());

        Long currentUserId = 2L;
        User currentUser = getUserById(currentUserId);

        Post parentPost = getPostById(contentId);

        Post comment = new Post();
        comment.setUser(currentUser);
        comment.setType(PostType.COMMENT);
        comment.setContent(request.getContent());
        comment.setHasMedia(MediaType.TEXT_ONLY);
        comment.setStatus(ContentStatus.NORMAL);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        comment.setParentPost(parentPost);

        if (request.getParentId() != null) {
            Post parentComment = getPostById(request.getParentId());
            comment.setParentPost(parentComment);
        }

        Post savedComment = postRepository.save(comment);
        return savedComment.getPid();
    }

    @Override
    public Object getComments(Long contentId, Integer page, Integer size) {
        log.info("获取评论列表: contentId={}, page={}, size={}", contentId, page, size);

        Post post = getPostById(contentId);
        List<Post> comments = postRepository.findByParentPostAndTypeAndStatusOrderByCreatedAtDesc(
                post, PostType.COMMENT, ContentStatus.NORMAL);

        int start = (page - 1) * size;
        int end = Math.min(start + size, comments.size());

        List<Post> pagedComments = new ArrayList<>();
        for (int i = start; i < end && i < comments.size(); i++) {
            pagedComments.add(comments.get(i));
        }

        List<Map<String, Object>> commentList = buildCommentTree(pagedComments);

        return new PageResponse<>(
                commentList,
                (long) comments.size(),
                page,
                size
        );
    }

    @Override
    @Transactional
    public Object likeContent(Long contentId) {
        log.info("点赞/取消点赞: contentId={}", contentId);

        Post post = getPostById(contentId);
        Long currentUserId = 2L;
        User currentUser = getUserById(currentUserId);

        Optional<PostLike> existingLike = postLikeRepository.findByPostAndUser(post, currentUser);
        Map<String, Object> result = new HashMap<>();

        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            result.put("liked", false);
        } else {
            PostLike postLike = new PostLike();
            postLike.setPost(post);
            postLike.setUser(currentUser);
            postLike.setCreatedAt(LocalDateTime.now());
            postLikeRepository.save(postLike);
            result.put("liked", true);
        }

        List<PostLike> likes = postLikeRepository.findByPost(post);
        result.put("count", likes.size());

        return result;
    }

    @Override
    public Object getLikes(Long contentId) {
        log.info("获取点赞列表: contentId={}", contentId);

        Post post = getPostById(contentId);
        List<PostLike> likes = postLikeRepository.findByPost(post);

        List<Map<String, Object>> likeList = likes.stream()
                .map(like -> {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("id", like.getUser().getUid());
                    userInfo.put("nickname", like.getUser().getNickname());
                    userInfo.put("avatarUrl", like.getUser().getAvatarUrl());
                    return userInfo;
                })
                .collect(Collectors.toList());

        return likeList;
    }

    @Override
    public Object searchByKeyword(String keyword, Integer page, Integer size, Integer type) {
        PostType postType;
        if (type != null && type == 1) {
            postType = PostType.COMMENT;
        } else {
            postType = PostType.POST;
        }

        List<Post> posts;
        if (keyword == null || keyword.trim().isEmpty()) {
            posts = postRepository.findByTypeAndStatusOrderByCreatedAtDesc(postType, ContentStatus.NORMAL);
        } else {
            posts = postRepository.findByContentContainingAndTypeAndStatusOrderByCreatedAtDesc(keyword.trim(), postType, ContentStatus.NORMAL);
        }

        int start = (page - 1) * size;
        int end = Math.min(start + size, posts.size());

        List<Post> pagedPosts = new ArrayList<>();
        for (int i = start; i < end && i < posts.size(); i++) {
            pagedPosts.add(posts.get(i));
        }

        List<Map<String, Object>> contentList = pagedPosts.stream()
                .map(this::convertToContentVO)
                .collect(Collectors.toList());

        return new PageResponse<>(
                contentList,
                (long) posts.size(),
                page,
                size
        );
    }

    private Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(1006, "动态不存在"));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(1002, "用户不存在"));
    }

    private Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(1005, "订单不存在"));
    }

    private Map<String, Object> convertToContentVO(Post post) {
        Map<String, Object> contentVO = new HashMap<>();
        contentVO.put("id", post.getPid());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", post.getUser().getUid());
        userInfo.put("nickname", post.getUser().getNickname());
        userInfo.put("avatarUrl", post.getUser().getAvatarUrl());
        contentVO.put("user", userInfo);

        contentVO.put("content", post.getContent());
        contentVO.put("mediaType", post.getHasMedia());
        contentVO.put("status", post.getStatus());
        contentVO.put("createdAt", post.getCreatedAt());
        contentVO.put("updatedAt", post.getUpdatedAt());

        if (post.getOrder() != null) {
            Map<String, Object> orderInfo = new HashMap<>();
            orderInfo.put("id", post.getOrder().getOid());
            orderInfo.put("activityType", post.getOrder().getActivityType());
            contentVO.put("order", orderInfo);
        }

        List<PostLike> likes = postLikeRepository.findByPost(post);
        List<Post> comments = postRepository.findByParentPostAndTypeAndStatusOrderByCreatedAtDesc(
                post, PostType.COMMENT, ContentStatus.NORMAL);

        contentVO.put("likeCount", likes.size());
        contentVO.put("commentCount", comments.size());

        return contentVO;
    }

    private Map<String, Object> convertToContentDetailVO(Post post) {
        Map<String, Object> detailVO = convertToContentVO(post);
        detailVO.put("type", post.getType());

        if (post.getOrder() != null) {
            Order order = post.getOrder();
            Map<String, Object> orderDetail = new HashMap<>();
            orderDetail.put("id", order.getOid());
            orderDetail.put("activityType", order.getActivityType());
            orderDetail.put("location", order.getLocation());
            orderDetail.put("startTime", order.getStartTime());
            orderDetail.put("status", order.getStatus());
            detailVO.put("order", orderDetail);
        }

        return detailVO;
    }

    private List<Map<String, Object>> buildCommentTree(List<Post> comments) {
        Map<Long, Map<String, Object>> commentMap = new HashMap<>();

        for (Post comment : comments) {
            Map<String, Object> commentVO = new HashMap<>();
            commentVO.put("id", comment.getPid());

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", comment.getUser().getUid());
            userInfo.put("nickname", comment.getUser().getNickname());
            userInfo.put("avatarUrl", comment.getUser().getAvatarUrl());
            commentVO.put("user", userInfo);

            commentVO.put("content", comment.getContent());
            commentVO.put("parentId", comment.getParentPost() != null ? comment.getParentPost().getPid() : null);
            commentVO.put("replies", new ArrayList<Map<String, Object>>());
            commentVO.put("createdAt", comment.getCreatedAt());

            commentMap.put(comment.getPid(), commentVO);
        }

        List<Map<String, Object>> rootComments = new ArrayList<>();

        for (Map<String, Object> commentVO : commentMap.values()) {
            Long parentId = (Long) commentVO.get("parentId");

            if (parentId == null) {
                rootComments.add(commentVO);
            } else if (commentMap.containsKey(parentId)) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> replies = (List<Map<String, Object>>) commentMap.get(parentId).get("replies");
                replies.add(commentVO);
            }
        }

        return rootComments;
    }
}