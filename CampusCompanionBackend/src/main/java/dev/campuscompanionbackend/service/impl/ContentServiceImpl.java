package dev.campuscompanionbackend.service.impl;

import dev.campuscompanionbackend.dto.request.CreateCommentRequest;
import dev.campuscompanionbackend.dto.request.CreateContentRequest;
import dev.campuscompanionbackend.dto.response.PageResponse;
import dev.campuscompanionbackend.entity.*;
import dev.campuscompanionbackend.enums.ContentStatus;
import dev.campuscompanionbackend.enums.MediaType;
import dev.campuscompanionbackend.enums.PostType;
import dev.campuscompanionbackend.exception.*;
import dev.campuscompanionbackend.repository.*;
import dev.campuscompanionbackend.service.ContentService;
import dev.campuscompanionbackend.service.FileService;
import dev.campuscompanionbackend.exception.FileDeleteFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
    private final PostMediaRepository postMediaRepository;
    private final FileService fileService;

    /**
     * 从当前 HTTP 请求头中解析出登录用户 ID。
     * 前端在 axios 拦截器中会把 localStorage.userId 放到 X-User-Id 头里。
     */
    private Long getCurrentUserIdOrThrow() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes servletAttributes)) {
            throw new ParamValidationFailedException("无法获取当前用户信息");
        }

        String userIdHeader = servletAttributes.getRequest().getHeader("X-User-Id");
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new ParamValidationFailedException("未登录或用户信息缺失");
        }

        try {
            return Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            throw new ParamValidationFailedException("用户信息格式错误", e);
        }
    }

    /**
     * 尝试从当前请求中获取登录用户 ID，如果不存在或格式不正确则返回 null。
     * 用于一些不强制要求登录的场景（例如浏览动态列表时判断是否已点赞）。
     */
    private Long getCurrentUserIdOrNull() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes servletAttributes)) {
            return null;
        }

        String userIdHeader = servletAttributes.getRequest().getHeader("X-User-Id");
        if (userIdHeader == null || userIdHeader.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            log.warn("X-User-Id 头格式错误: {}", userIdHeader);
            return null;
        }
    }

    @Override
    @Transactional
    public Long createContent(CreateContentRequest request) {
        log.info("发布动态: content={}, mediaType={}, orderId={}",
                request.getContent().substring(0, Math.min(50, request.getContent().length())),
                request.getMediaType(), request.getOrderId());

        Long currentUserId = getCurrentUserIdOrThrow();
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
    public Long updateContent(Long contentId, CreateContentRequest request) {
        log.info("修改动态: contentId={}", contentId);

        Post post = getPostById(contentId);

        if (post.getType() != PostType.POST) {
            log.error("修改非动态类型");
        } else {
            if (request.getContent() != null) {
                post.setContent(request.getContent());
            }

            if (request.getMediaType() != null) {
                post.setHasMedia(request.getMediaType());
            }

            if (request.getOrderId() != null) {
                post.setOrder(getOrderById(request.getOrderId()));
            }

            post.setUpdatedAt(LocalDateTime.now());
            postRepository.save(post);
        }

        return contentId;
    }

    @Override
    @Transactional
    public void deleteContent(Long contentId) {
        Post post = getPostById(contentId);

        Long currentUserId = getCurrentUserIdOrThrow();
        if (!post.getUser().getUid().equals(currentUserId)) {
            throw new SomethingHappenedException(
                    String.format("非动态发布者尝试删除动态: userId=%d, contentId=%d", post.getUser().getUid(), contentId)
            );
        }

        // 如果该动态包含媒体文件，删除这些文件并移除数据库记录
        if (post.getHasMedia() != null && post.getHasMedia() != MediaType.TEXT_ONLY) {
            List<PostMedia> medias = postMediaRepository.findByPost(post);
            for (PostMedia pm : medias) {
                try {
                    // 物理删除文件（fileService 会根据配置定位到上传目录）
                    fileService.deleteFile(pm.getUrl());
                } catch (FileDeleteFailedException e) {
                    // 记录警告但继续其他文件删除
                    log.warn("删除媒体文件失败, pmid={}, url={}, error={}", pm.getPmid(), pm.getUrl(), e.getMessage());
                }
            }
            // 从数据库中删除媒体记录
            postMediaRepository.deleteAll(medias);
        }

        // 标记动态为已删除
        post.setStatus(ContentStatus.DELETED);
        post.setUpdatedAt(LocalDateTime.now());
        log.info("删除动态: contentId={}", contentId);
        postRepository.save(post);
    }

    @Override
    @Transactional
    public String uploadMedia(Long contentId, MultipartFile media) {
        log.info("上传媒体文件: contentId={}, filename={}", contentId, media.getOriginalFilename());

        Post post = getPostById(contentId);

        String contentType = media.getContentType();
        if (contentType == null) {
            throw new FileUploadFailedException("无法识别的文件类型: contentId=" + contentId);
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
            throw new FileUploadFailedException("不支持图片和视频以外类型的文件: contentId=" + contentId);
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

            // 记录媒体信息，便于后续列表/详情接口返回 mediaUrls
            PostMedia postMedia = new PostMedia();
            postMedia.setPost(post);
            postMedia.setMediaType(mediaType);
            postMedia.setUrl(mediaUrl);
            postMediaRepository.save(postMedia);

            return mediaUrl;
        } catch (IOException e) {
            log.error("上传媒体文件失败", e);
            throw new FileUploadFailedException("上传媒体文件失败: contentId=" + contentId, e);
        }
    }

    @Override
    @Transactional
    public List<PostMedia> getMedias(Long contentId) {
        log.info("获取动态所有媒体文件: contentId={}", contentId);

        Post post = getPostById(contentId);

        if (post.getType() != PostType.POST) {
            log.error("获取非动态媒体文件");
        } else if (post.getHasMedia() == MediaType.TEXT_ONLY) {
            log.error("获取纯文本动态媒体文件");
        } else {
            return postMediaRepository.findByPost(post);
        }

        return null;
    }

    @Override
    @Transactional
    public Long createComment(Long contentId, CreateCommentRequest request) {
        log.info("发布评论: contentId={}, parentId={}", contentId, request.getParentId());
        Long currentUserId = getCurrentUserIdOrThrow();
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
        // 1. 先查出这条动态下的所有「顶层评论」（直接回复动态的评论）
        List<Post> rootComments = postRepository.findByParentPostAndTypeAndStatusOrderByCreatedAtDesc(
                post, PostType.COMMENT, ContentStatus.NORMAL);

        // 2. 对顶层评论做分页（树形结构通常只对根节点分页，子回复全部带回）
        int start = (page - 1) * size;
        int end = Math.min(start + size, rootComments.size());

        List<Post> pagedRootComments = new ArrayList<>();
        for (int i = start; i < end && i < rootComments.size(); i++) {
            pagedRootComments.add(rootComments.get(i));
        }

        // 3. 为每条顶层评论递归查询其所有子评论，构建完整树
        List<Map<String, Object>> commentList = new ArrayList<>();
        for (Post root : pagedRootComments) {
            commentList.add(buildCommentNode(root, null));
        }

        // total 统计顶层评论总数，符合前端分页语义
        return new PageResponse<>(
                commentList,
                (long) rootComments.size(),
                page,
                size
        );
    }

    @Override
    @Transactional
    public Map<String, Object> likeContent(Long contentId) {
        log.info("点赞/取消点赞: contentId={}", contentId);

        Post post = getPostById(contentId);
        Long currentUserId = getCurrentUserIdOrThrow();
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

        return likes.stream()
                .map(like -> {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("id", like.getUser().getUid());
                    userInfo.put("nickname", like.getUser().getNickname());
                    userInfo.put("avatarUrl", like.getUser().getAvatarUrl());
                    return userInfo;
                })
                .collect(Collectors.toList());
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
                .orElseThrow(() -> new ContentNotExistException("动态不存在: postId=" + postId));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotExistException("用户不存在: userId=" + userId));
    }

    private Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotExistException("订单不存在: orderId=" + orderId));
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

        // 媒体资源列表
        List<PostMedia> mediaList = postMediaRepository.findByPost(post);
        if (mediaList != null && !mediaList.isEmpty()) {
            List<String> mediaUrls = mediaList.stream()
                .map(PostMedia::getUrl)
                .collect(Collectors.toList());
            contentVO.put("mediaUrls", mediaUrls);
        }

        // 标记当前登录用户是否已点赞该内容，用于前端在刷新后还原点赞状态
        boolean liked = false;
        Long currentUserId = getCurrentUserIdOrNull();
        if (currentUserId != null) {
            try {
                User currentUser = getUserById(currentUserId);
                liked = postLikeRepository.findByPostAndUser(post, currentUser).isPresent();
            } catch (BusinessException ex) {
                // 如果用户不存在等异常，这里只记录日志，不影响列表正常返回
                log.warn("判断点赞状态时获取当前用户失败: {}", ex.getMessage());
            }
        }
        contentVO.put("liked", liked);

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

    /**
     * 递归构建评论节点及其所有子评论
     *
     * @param comment  当前评论实体
     * @param parentId 父评论 ID，顶层评论为 null
     * @return 包含 id/user/content/parentId/replies/createdAt 的评论 VO
     */
    private Map<String, Object> buildCommentNode(Post comment, Long parentId) {
        Map<String, Object> commentVO = new HashMap<>();
        commentVO.put("id", comment.getPid());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", comment.getUser().getUid());
        userInfo.put("nickname", comment.getUser().getNickname());
        userInfo.put("avatarUrl", comment.getUser().getAvatarUrl());
        commentVO.put("user", userInfo);

        commentVO.put("content", comment.getContent());
        commentVO.put("parentId", parentId);
        commentVO.put("createdAt", comment.getCreatedAt());

        // 查询当前评论的直接子评论，并递归构建
        List<Post> childComments = postRepository.findByParentPostAndTypeAndStatusOrderByCreatedAtDesc(
                comment, PostType.COMMENT, ContentStatus.NORMAL);
        List<Map<String, Object>> replies = new ArrayList<>();
        for (Post child : childComments) {
            replies.add(buildCommentNode(child, comment.getPid()));
        }
        commentVO.put("replies", replies);

        return commentVO;
    }
}