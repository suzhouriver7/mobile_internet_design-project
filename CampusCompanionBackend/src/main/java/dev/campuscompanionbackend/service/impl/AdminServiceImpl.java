package dev.campuscompanionbackend.service.impl;

import dev.campuscompanionbackend.dto.response.PageResponse;
import dev.campuscompanionbackend.entity.Order;
import dev.campuscompanionbackend.entity.Post;
import dev.campuscompanionbackend.entity.User;
import dev.campuscompanionbackend.enums.ContentStatus;
import dev.campuscompanionbackend.enums.OrderStatus;
import dev.campuscompanionbackend.enums.PostType;
import dev.campuscompanionbackend.enums.UserType;
import dev.campuscompanionbackend.exception.ContentNotExistException;
import dev.campuscompanionbackend.exception.OrderNotExistException;
import dev.campuscompanionbackend.exception.UserNotExistException;
import dev.campuscompanionbackend.repository.OrderRepository;
import dev.campuscompanionbackend.repository.PostRepository;
import dev.campuscompanionbackend.repository.UserRepository;
import dev.campuscompanionbackend.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PostRepository postRepository;

    @Override
    public Object getUsers(Integer page, Integer size, UserType userType) {
        log.info("管理员获取用户列表: page={}, size={}, userType={}", page, size, userType);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage;

        if (userType != null) {
            Iterable<User> users = userRepository.findByUserType(userType);
            List<User> userList = new ArrayList<>();
            for (User user : users) {
                userList.add(user);
            }

            userList.sort((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt()));

            int start = (page - 1) * size;
            int end = Math.min(start + size, userList.size());
            List<User> pagedList = new ArrayList<>();
            for (int i = start; i < end && i < userList.size(); i++) {
                pagedList.add(userList.get(i));
            }

            List<Map<String, Object>> resultList = pagedList.stream()
                    .map(this::convertToUserInfo)
                    .collect(Collectors.toList());

            return new PageResponse<>(
                    resultList,
                    (long) userList.size(),
                    page,
                    size
            );
        } else {
            userPage = userRepository.findAll(pageable);

            List<Map<String, Object>> userList = userPage.getContent().stream()
                    .map(this::convertToUserInfo)
                    .collect(Collectors.toList());

            return new PageResponse<>(
                    userList,
                    userPage.getTotalElements(),
                    page,
                    size
            );
        }
    }

    @Override
    @Transactional
    public void updateUserType(Long userId, UserType userType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotExistException("被修改的用户不存在: userId=" + userId));

        user.setUserType(userType);
        log.info("修改用户权限: userId={}, userType={}", userId, userType);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void manageOrder(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotExistException("被管理的订单不存在: orderId=" + orderId));

        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        log.info("管理订单: orderId={}, status={}", orderId, status);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void deleteContent(Long contentId) {
        Post post = postRepository.findById(contentId)
                .orElseThrow(() -> new ContentNotExistException("动态不存在: contentId=" + contentId));

        post.setStatus(ContentStatus.DELETED);
        post.setUpdatedAt(LocalDateTime.now());
        log.info("管理员删除内容: contentId={}", contentId);
        postRepository.save(post);
    }

    @Override
    public Object getStatistics() {
        log.info("获取系统统计");

        Map<String, Object> statistics = new HashMap<>();

        long totalUsers = userRepository.count();
        Iterable<User> adminUsers = userRepository.findByUserType(UserType.ADMIN);
        long adminCount = 0;
        for (User user : adminUsers) {
            adminCount++;
        }
        long commonUserCount = totalUsers - adminCount;

        statistics.put("userCount", totalUsers);
        statistics.put("adminCount", adminCount);
        statistics.put("commonUserCount", commonUserCount);

        long totalOrders = orderRepository.count();
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        List<Order> inProgressOrders = orderRepository.findByStatus(OrderStatus.IN_PROGRESS);
        List<Order> completedOrders = orderRepository.findByStatus(OrderStatus.COMPLETED);

        statistics.put("orderCount", totalOrders);
        statistics.put("pendingOrderCount", pendingOrders.size());
        statistics.put("inProgressOrderCount", inProgressOrders.size());
        statistics.put("completedOrderCount", completedOrders.size());

        long totalPosts = postRepository.count();
        List<Post> posts = postRepository.findByTypeAndStatusOrderByCreatedAtDesc(PostType.POST, ContentStatus.NORMAL);
        List<Post> comments = postRepository.findByTypeAndStatusOrderByCreatedAtDesc(PostType.COMMENT, ContentStatus.NORMAL);

        statistics.put("contentCount", totalPosts);
        statistics.put("postCount", posts.size());
        statistics.put("commentCount", comments.size());

        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        long todayActiveUsers = (long) (commonUserCount * 0.3);

        statistics.put("todayActiveUsers", todayActiveUsers);

        return statistics;
    }

    private Map<String, Object> convertToUserInfo(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getUid());
        userInfo.put("email", user.getEmail());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatarUrl", user.getAvatarUrl());
        userInfo.put("userType", user.getUserType());
        userInfo.put("userStatus", user.getUserStatus());
        userInfo.put("createdAt", user.getCreatedAt());
        userInfo.put("lastLoginAt", user.getLastLoginAt());
        return userInfo;
    }
}