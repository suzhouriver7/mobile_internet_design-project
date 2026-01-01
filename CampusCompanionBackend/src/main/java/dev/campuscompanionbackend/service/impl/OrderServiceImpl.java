package dev.campuscompanionbackend.service.impl;

import dev.campuscompanionbackend.dto.request.ApplyOrderRequest;
import dev.campuscompanionbackend.dto.request.CreateOrderRequest;
import dev.campuscompanionbackend.dto.response.PageResponse;
import dev.campuscompanionbackend.entity.*;
import dev.campuscompanionbackend.enums.ActivityType;
import dev.campuscompanionbackend.enums.ApplyStatus;
import dev.campuscompanionbackend.enums.Campus;
import dev.campuscompanionbackend.enums.OrderStatus;
import dev.campuscompanionbackend.enums.UserType;
import dev.campuscompanionbackend.exception.*;
import dev.campuscompanionbackend.repository.*;
import dev.campuscompanionbackend.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderApplyRepository orderApplyRepository;
    private final OrderMessageRepository orderMessageRepository;
    private final OrderAcceptRepository orderAcceptRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

    @Override
    @Transactional
    public Long createOrder(CreateOrderRequest request) {
        Long currentUserId = getCurrentUserIdOrThrow();
        User currentUser = getUserById(currentUserId);

        LocalDateTime startTime = LocalDateTime.parse(request.getStartTime(), formatter);

        Order order = new Order();
        order.setUser(currentUser);
        order.setActivityType(request.getActivityType());
        order.setGenderRequire(request.getGenderRequire());
        order.setCampus(request.getCampus());
        order.setLocation(request.getLocation());
        order.setStartTime(startTime);
        order.setNote(request.getNote());
        order.setMaxPeople(request.getMaxPeople().byteValue());
        order.setCurrentPeople((byte) 1);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        log.info("发布预约订单: activityType={}, campus={}", request.getActivityType(), request.getCampus());
        return savedOrder.getOid();
    }

    @Override
    public Object getOrders(Integer page, Integer size, OrderStatus status,
                            ActivityType activityType, Campus campus) {
        log.info("获取订单列表: page={}, size={}, status={}, activityType={}, campus={}",
                page, size, status, activityType, campus);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 如果没有任何筛选条件，直接使用分页查询
        if (status == null && activityType == null && campus == null) {
            Page<Order> orderPage = orderRepository.findAll(pageable);

            List<Map<String, Object>> orderList = orderPage.getContent().stream()
                    .map(this::convertToOrderVO)
                    .collect(Collectors.toList());

            return new PageResponse<>(
                    orderList,
                    orderPage.getTotalElements(),
                    page,
                    size
            );
        }

        // 其余情况统一走内存筛选，支持任意条件组合
        List<Order> baseList;
        if (status != null && activityType != null && campus != null) {
            // 三个条件都有时优先走已有的组合查询
            baseList = orderRepository.findByCampusAndActivityTypeAndStatus(campus, activityType, status);
        } else if (status != null) {
            // 只有状态时可以复用按状态查询
            baseList = orderRepository.findByStatus(status);
        } else {
            // 其它组合场景先按创建时间倒序查出全部，再在内存中过滤
            baseList = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        List<Order> filtered = baseList.stream()
                .filter(o -> status == null || o.getStatus() == status)
                .filter(o -> activityType == null || o.getActivityType() == activityType)
                .filter(o -> campus == null || o.getCampus() == campus)
                .collect(Collectors.toList());

        return manualPagination(filtered, page, size);
    }

    private Object manualPagination(List<Order> orders, Integer page, Integer size) {
        int start = (page - 1) * size;
        int end = Math.min(start + size, orders.size());

        List<Order> pagedOrders = new ArrayList<>();
        for (int i = start; i < end && i < orders.size(); i++) {
            pagedOrders.add(orders.get(i));
        }

        List<Map<String, Object>> orderList = pagedOrders.stream()
                .map(this::convertToOrderVO)
                .collect(Collectors.toList());

        return new PageResponse<>(
                orderList,
                (long) orders.size(),
                page,
                size
        );
    }

    @Override
    public Object getOrderDetail(Long orderId) {
        log.info("获取订单详情: orderId={}", orderId);

        Order order = getOrderById(orderId);

        Map<String, Object> orderDetail = new HashMap<>();
        orderDetail.put("order", convertToOrderVO(order));

        List<OrderApply> applications = orderApplyRepository.findByOrder(order);
        List<Map<String, Object>> applicationList = applications.stream()
                .map(this::convertToOrderApplyVO)
                .collect(Collectors.toList());
        orderDetail.put("applications", applicationList);

        return orderDetail;
    }

    @Override
    @Transactional
    public Long updateOrder(Long orderId, CreateOrderRequest request) {
        Order order = getOrderById(orderId);

        isOrderPendingOrInProcess(orderId, order);

        if (request.getActivityType() != null) {
            order.setActivityType(request.getActivityType());
        }
        if (request.getGenderRequire() != null) {
            order.setGenderRequire(request.getGenderRequire());
        }
        if (request.getCampus() != null) {
            order.setCampus(request.getCampus());
        }
        if (request.getLocation() != null && !request.getLocation().isEmpty()) {
            order.setLocation(request.getLocation());
        }
        if (request.getStartTime() != null && !request.getStartTime().isEmpty()) {
            order.setStartTime(LocalDateTime.parse(request.getStartTime(), formatter));
        }
        if (request.getNote() != null) {
            order.setNote(request.getNote());
        }
        if (request.getMaxPeople() != null) {
            order.setMaxPeople(request.getMaxPeople().byteValue());
        }

        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("修改订单信息: orderId={}", orderId);
        return orderId;
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = getOrderById(orderId);
        if(order == null) {
            return;
        }

        Long currentUserId = getCurrentUserIdOrThrow();
        User currentUser = getUserById(currentUserId);
        boolean isOwner = order.getUser().getUid().equals(currentUserId);
        boolean isAdmin = currentUser.getUserType() == UserType.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new NoPermissionException("只有订单发布者或管理员可以删除订单");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        log.info("删除订单: orderId={}", orderId);
    }

    @Override
    @Transactional
    public void completeOrder(Long orderId) {
        Order order = getOrderById(orderId);
        if(order == null){
            throw new OrderNotExistException("订单不存在: orderId=" + orderId);
        }
        order.setStatus(OrderStatus.COMPLETED);
        order.setUpdatedAt(LocalDateTime.now());
        log.info("完成订单: orderId={}", orderId);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public Long applyOrder(Long orderId, ApplyOrderRequest request) {
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new OrderNotExistException("订单不存在: orderId=" + orderId);
        }
        isOrderPendingOrInProcess(orderId, order);

        if (order.getCurrentPeople() >= order.getMaxPeople()) {
            throw new OrderFullException("人数已满: orderId=" + orderId);
        }

        Long currentUserId = getCurrentUserIdOrThrow();
        User currentUser = getUserById(currentUserId);

        // 一个用户对同一订单只能有一条申请记录
        // 如果之前申请过且未撤销，则不允许重复申请；
        // 如果之前是已撤销状态，则视为重新申请，直接将状态改回 PENDING_REVIEW。
        Optional<OrderApply> optionalApply = orderApplyRepository.findByOrderAndUser(order, currentUser);

        OrderApply targetApply;
        if (optionalApply.isPresent()) {
            OrderApply existing = optionalApply.get();
            if (existing.getStatus() != ApplyStatus.CANCELLED_APPLY) {
                throw new DuplicateOperationException("已申请过该订单");
            }
            // 之前撤销过，现在重新申请，复用原记录
            existing.setStatus(ApplyStatus.PENDING_REVIEW);
            targetApply = existing;
        } else {
            OrderApply orderApply = new OrderApply();
            orderApply.setOrder(order);
            orderApply.setUser(currentUser);
            orderApply.setStatus(ApplyStatus.PENDING_REVIEW);
            targetApply = orderApply;
        }

        OrderApply savedApply = orderApplyRepository.save(targetApply);
        log.info("申请加入订单: orderId={}", orderId);
        return savedApply.getApid();
    }

    private void isOrderPendingOrInProcess(Long orderId, Order order) {
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new OrderCompletedException("订单已完成，无法修改: orderId=" + orderId);
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderCompletedException("订单已取消，无法修改: orderId=" + orderId);
        }
        if (order.getStatus() == OrderStatus.EXPIRED) {
            throw new OrderExpiredException("订单已过期，无法修改: orderId=" + orderId);
        }
    }

    @Override
    @Transactional
    public void cancelApply(Long orderId) {
        Order order = getOrderById(orderId);

        if (order == null){
            throw new OrderNotExistException("订单不存在: orderId=" + orderId);
        }

        Long currentUserId = getCurrentUserIdOrThrow();
        User currentUser = getUserById(currentUserId);

        OrderApply orderApply = orderApplyRepository.findByOrderAndUser(order, currentUser)
                .orElseThrow(() -> new ApplicationNotExistException("申请记录不存在"));

        orderApply.setStatus(ApplyStatus.CANCELLED_APPLY);
        orderApplyRepository.save(orderApply);
        log.info("取消申请: orderId={}", orderId);
    }

    @Override
    public Object getApplications(Long orderId) {
        log.info("获取申请列表: orderId={}", orderId);

        Order order = getOrderById(orderId);
        List<OrderApply> applications = orderApplyRepository.findByOrder(order);

        return applications.stream()
                .map(this::convertToOrderApplyVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void auditApply(Long applyId, ApplyStatus status) {
        OrderApply orderApply = orderApplyRepository.findById(applyId)
                .orElseThrow(() -> new ApplicationNotExistException("申请记录不存在"));

        Order order = orderApply.getOrder();
        Long currentUserId = getCurrentUserIdOrThrow();
        if (!order.getUser().getUid().equals(currentUserId)) {
            throw new NoPermissionException("只有订单发布者可以审核申请");
        }

        if (status == ApplyStatus.APPROVED) {
            byte currentPeople = order.getCurrentPeople();
            if (currentPeople < order.getMaxPeople()) {
                order.setCurrentPeople((byte) (currentPeople + 1));
                orderRepository.save(order);
            }else{
                throw new OrderFullException("订单已满: orderId=" + order.getOid());
            }
        }
        orderApply.setStatus(status);
        orderApplyRepository.save(orderApply);
        log.info("审核申请: applyId={}, status={}", applyId, status);
    }

    @Override
    @Transactional
    public void acceptOrder(Long orderId, Long accepterId) {
        log.info("接受订单: orderId={}, accepterId={}", orderId, accepterId);

        Order order = getOrderById(orderId);
        User accepter = getUserById(accepterId);

        if (orderAcceptRepository.existsByOrder(order)) {
            throw new DuplicateOperationException("订单已被接受");
        }

        OrderAccept orderAccept = new OrderAccept();
        orderAccept.setOrder(order);
        orderAccept.setAccepter(accepter);
        orderAccept.setAcceptedAt(LocalDateTime.now());
        orderAcceptRepository.save(orderAccept);

        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public Long sendOrderMessage(Long orderId, String content) {
        log.info("发送订单消息: orderId={}", orderId);

        Order order = getOrderById(orderId);

        Long senderId = getCurrentUserIdOrThrow();
        User sender = getUserById(senderId);

        // 简化处理：将消息发送给订单发布者
        User receiver = order.getUser();

        OrderMessage orderMessage = new OrderMessage();
        orderMessage.setOrder(order);
        orderMessage.setSender(sender);
        orderMessage.setReceiver(receiver);
        orderMessage.setContent(content);
        orderMessage.setCreatedAt(LocalDateTime.now());

        OrderMessage savedMessage = orderMessageRepository.save(orderMessage);
        return savedMessage.getMid();
    }

    @Override
    public Object getOrderMessages(Long orderId, Integer page, Integer size) {
        log.info("获取订单消息: orderId={}, page={}, size={}", orderId, page, size);

        Order order = getOrderById(orderId);
        List<OrderMessage> messages = orderMessageRepository.findByOrderOrderByCreatedAtAsc(order);

        int start = (page - 1) * size;
        int end = Math.min(start + size, messages.size());

        List<OrderMessage> pagedMessages = new ArrayList<>();
        for (int i = start; i < end && i < messages.size(); i++) {
            pagedMessages.add(messages.get(i));
        }

        List<Map<String, Object>> messageList = pagedMessages.stream()
                .map(this::convertToOrderMessageVO)
                .collect(Collectors.toList());

        return new PageResponse<>(
                messageList,
                (long) messages.size(),
                page,
                size
        );
    }

    private Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotExistException("订单不存在"));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotExistException("用户不存在"));
    }

    private Map<String, Object> convertToOrderVO(Order order) {
        Map<String, Object> orderVO = new HashMap<>();
        orderVO.put("id", order.getOid());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", order.getUser().getUid());
        userInfo.put("nickname", order.getUser().getNickname());
        userInfo.put("avatarUrl", order.getUser().getAvatarUrl());
        orderVO.put("user", userInfo);

        orderVO.put("activityType", order.getActivityType());
        orderVO.put("genderRequire", order.getGenderRequire());
        orderVO.put("campus", order.getCampus());
        orderVO.put("location", order.getLocation());
        orderVO.put("startTime", order.getStartTime());
        orderVO.put("note", order.getNote());
        orderVO.put("maxPeople", order.getMaxPeople());
        orderVO.put("currentPeople", order.getCurrentPeople());
        orderVO.put("status", order.getStatus());
        orderVO.put("createdAt", order.getCreatedAt());
        orderVO.put("updatedAt", order.getUpdatedAt());

        return orderVO;
    }

    private Map<String, Object> convertToOrderApplyVO(OrderApply orderApply) {
        Map<String, Object> applyVO = new HashMap<>();
        applyVO.put("id", orderApply.getApid());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", orderApply.getUser().getUid());
        userInfo.put("nickname", orderApply.getUser().getNickname());
        userInfo.put("avatarUrl", orderApply.getUser().getAvatarUrl());
        applyVO.put("user", userInfo);

        applyVO.put("status", orderApply.getStatus());
        applyVO.put("createdAt", orderApply.getCreatedAt());

        return applyVO;
    }

    private Map<String, Object> convertToOrderMessageVO(OrderMessage message) {
        Map<String, Object> messageVO = new HashMap<>();
        messageVO.put("id", message.getMid());

        Map<String, Object> senderInfo = new HashMap<>();
        senderInfo.put("id", message.getSender().getUid());
        senderInfo.put("nickname", message.getSender().getNickname());
        senderInfo.put("avatarUrl", message.getSender().getAvatarUrl());
        messageVO.put("sender", senderInfo);

        Map<String, Object> receiverInfo = new HashMap<>();
        receiverInfo.put("id", message.getReceiver().getUid());
        receiverInfo.put("nickname", message.getReceiver().getNickname());
        receiverInfo.put("avatarUrl", message.getReceiver().getAvatarUrl());
        messageVO.put("receiver", receiverInfo);

        messageVO.put("content", message.getContent());
        messageVO.put("createdAt", message.getCreatedAt());

        return messageVO;
    }
}