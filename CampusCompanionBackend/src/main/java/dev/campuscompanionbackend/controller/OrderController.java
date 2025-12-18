package dev.campuscompanionbackend.controller;

import dev.campuscompanionbackend.dto.request.AcceptOrderRequest;
import dev.campuscompanionbackend.dto.request.ApplyOrderRequest;
import dev.campuscompanionbackend.dto.request.AuditApplyRequest;
import dev.campuscompanionbackend.dto.request.CreateOrderRequest;
import dev.campuscompanionbackend.dto.request.SendOrderMessageRequest;
import dev.campuscompanionbackend.dto.response.ApiResponse;
import dev.campuscompanionbackend.enums.ActivityType;
import dev.campuscompanionbackend.enums.Campus;
import dev.campuscompanionbackend.enums.OrderStatus;
import dev.campuscompanionbackend.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单管理相关接口
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController extends BaseController {
    
    private final OrderService orderService;
    
    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    /**
     * 发布预约订单
     * @param request 发布订单请求参数
     * @return ApiResponse<Long>
     */
    @PostMapping
    public ApiResponse<Long> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Long orderId = orderService.createOrder(request);
        return success("发布成功", orderId);
    }
    
    /**
     * 获取订单列表
     * @param page 页码，默认1
     * @param size 每页数量，默认10
     * @param status 订单状态
     * @param activityType 活动类型
     * @param campus 校区
     * @return ApiResponse<Object>
     */
    @GetMapping
    public ApiResponse<Object> getOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) ActivityType activityType,
            @RequestParam(required = false) Campus campus) {
        Object result = orderService.getOrders(page, size, status, activityType, campus);
        return success(result);
    }
    
    /**
     * 获取订单详情
     * @param orderId 订单ID
     * @return ApiResponse<Object>
     */
    @GetMapping("/{orderId}")
    public ApiResponse<Object> getOrderDetail(@PathVariable Long orderId) {
        Object detail = orderService.getOrderDetail(orderId);
        return success(detail);
    }
    
    /**
     * 修改订单信息
     * @param orderId 订单ID
     * @param request 修改订单请求参数
     * @return ApiResponse<Long>
     */
    @PutMapping("/{orderId}")
    public ApiResponse<Long> updateOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody CreateOrderRequest request) {
        Long updatedOrderId = orderService.updateOrder(orderId, request);
        return success("修改成功", updatedOrderId);
    }
    
    /**
     * 删除订单
     * @param orderId 订单ID
     * @return ApiResponse<Void>
     */
    @DeleteMapping("/{orderId}")
    public ApiResponse<Void> deleteOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        return success("删除成功", null);
    }
    
    /**
     * 完成订单
     * @param orderId 订单ID
     * @return ApiResponse<Void>
     */
    @PutMapping("/{orderId}/complete")
    public ApiResponse<Void> completeOrder(@PathVariable Long orderId) {
        orderService.completeOrder(orderId);
        return success("订单已完成", null);
    }
    
    /**
     * 申请加入订单
     * @param orderId 订单ID
     * @param request 申请加入订单请求参数
     * @return ApiResponse<Long>
     */
    @PostMapping("/{orderId}/apply")
    public ApiResponse<Long> applyOrder(
            @PathVariable Long orderId,
            @RequestBody ApplyOrderRequest request) {
        Long applyId = orderService.applyOrder(orderId, request);
        return success("申请成功", applyId);
    }
    
    /**
     * 取消申请
     * @param orderId 订单ID
     * @return ApiResponse<Void>
     */
    @DeleteMapping("/{orderId}/apply")
    public ApiResponse<Void> cancelApply(@PathVariable Long orderId) {
        orderService.cancelApply(orderId);
        return success("取消成功", null);
    }
    
    /**
     * 获取申请列表
     * @param orderId 订单ID
     * @return ApiResponse<Object>
     */
    @GetMapping("/{orderId}/applications")
    public ApiResponse<Object> getApplications(@PathVariable Long orderId) {
        Object applications = orderService.getApplications(orderId);
        return success(applications);
    }
    
    /**
     * 审核申请
     * @param applyId 申请记录ID
     * @param request 审核申请请求参数
     * @return ApiResponse<Void>
     */
    @PutMapping("/applications/{applyId}")
    public ApiResponse<Void> auditApply(
            @PathVariable Long applyId,
            @Valid @RequestBody AuditApplyRequest request) {
        orderService.auditApply(applyId, request.getStatus());
        return success("审核成功", null);
    }
    
    /**
     * 接受订单
     * @param orderId 订单ID
     * @param request 接受订单请求参数
     * @return ApiResponse<Void>
     */
    @PostMapping("/{orderId}/accept")
    public ApiResponse<Void> acceptOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody AcceptOrderRequest request) {
        orderService.acceptOrder(orderId, request.getAccepterId());
        return success("接受成功", null);
    }
    
    /**
     * 发送订单消息
     * @param orderId 订单ID
     * @param request 发送订单消息请求参数
     * @return ApiResponse<Long>
     */
    @PostMapping("/{orderId}/messages")
    public ApiResponse<Long> sendOrderMessage(
            @PathVariable Long orderId,
            @Valid @RequestBody SendOrderMessageRequest request) {
        Long messageId = orderService.sendOrderMessage(orderId, request.getContent());
        return success("发送成功", messageId);
    }
    
    /**
     * 获取订单消息
     * @param orderId 订单ID
     * @param page 页码，默认1
     * @param size 每页数量，默认20
     * @return ApiResponse<Object>
     */
    @GetMapping("/{orderId}/messages")
    public ApiResponse<Object> getOrderMessages(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Object messages = orderService.getOrderMessages(orderId, page, size);
        return success(messages);
    }
}