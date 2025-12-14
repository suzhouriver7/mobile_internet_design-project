package dev.campuscompanionbackend.service;

import dev.campuscompanionbackend.dto.request.AcceptOrderRequest;
import dev.campuscompanionbackend.dto.request.ApplyOrderRequest;
import dev.campuscompanionbackend.dto.request.CreateOrderRequest;
import dev.campuscompanionbackend.dto.request.SendOrderMessageRequest;
import dev.campuscompanionbackend.enums.ActivityType;
import dev.campuscompanionbackend.enums.ApplyStatus;
import dev.campuscompanionbackend.enums.Campus;
import dev.campuscompanionbackend.enums.OrderStatus;

/**
 * 订单服务接口
 */
public interface OrderService {
    
    /**
     * 发布预约订单
     * @param request 发布订单请求参数
     * @return Long 订单ID
     */
    Long createOrder(CreateOrderRequest request);
    
    /**
     * 获取订单列表
     * @param page 页码
     * @param size 每页数量
     * @param status 订单状态
     * @param activityType 活动类型
     * @param campus 校区
     * @return Object 分页订单列表
     */
    Object getOrders(Integer page, Integer size, OrderStatus status, ActivityType activityType, Campus campus);
    
    /**
     * 获取订单详情
     * @param orderId 订单ID
     * @return Object 订单详情
     */
    Object getOrderDetail(Long orderId);
    
    /**
     * 修改订单信息
     * @param orderId 订单ID
     * @param request 修改订单请求参数
     * @return Long 订单ID
     */
    Long updateOrder(Long orderId, CreateOrderRequest request);
    
    /**
     * 删除订单
     * @param orderId 订单ID
     */
    void deleteOrder(Long orderId);
    
    /**
     * 完成订单
     * @param orderId 订单ID
     */
    void completeOrder(Long orderId);
    
    /**
     * 申请加入订单
     * @param orderId 订单ID
     * @param request 申请加入订单请求参数
     * @return Long 申请记录ID
     */
    Long applyOrder(Long orderId, ApplyOrderRequest request);
    
    /**
     * 取消申请
     * @param orderId 订单ID
     */
    void cancelApply(Long orderId);
    
    /**
     * 获取申请列表
     * @param orderId 订单ID
     * @return Object 申请列表
     */
    Object getApplications(Long orderId);
    
    /**
     * 审核申请
     * @param applyId 申请记录ID
     * @param status 审核状态
     */
    void auditApply(Long applyId, ApplyStatus status);
    
    /**
     * 接受订单
     * @param orderId 订单ID
     * @param accepterId 被接受的用户ID
     */
    void acceptOrder(Long orderId, Long accepterId);
    
    /**
     * 发送订单消息
     * @param orderId 订单ID
     * @param content 消息内容
     * @return Long 消息ID
     */
    Long sendOrderMessage(Long orderId, String content);
    
    /**
     * 获取订单消息
     * @param orderId 订单ID
     * @param page 页码
     * @param size 每页数量
     * @return Object 分页消息列表
     */
    Object getOrderMessages(Long orderId, Integer page, Integer size);
}