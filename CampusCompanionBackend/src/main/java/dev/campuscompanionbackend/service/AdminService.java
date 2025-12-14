package dev.campuscompanionbackend.service;

import dev.campuscompanionbackend.enums.OrderStatus;
import dev.campuscompanionbackend.enums.UserType;

/**
 * 管理员服务接口
 */
public interface AdminService {
    
    /**
     * 获取用户列表（管理员）
     * @param page 页码
     * @param size 每页数量
     * @param userType 用户类型
     * @return Object 分页用户列表
     */
    Object getUsers(Integer page, Integer size, UserType userType);
    
    /**
     * 修改用户权限
     * @param userId 用户ID
     * @param userType 用户类型
     */
    void updateUserType(Long userId, UserType userType);
    
    /**
     * 管理订单
     * @param orderId 订单ID
     * @param status 订单状态
     */
    void manageOrder(Long orderId, OrderStatus status);
    
    /**
     * 删除任意内容
     * @param contentId 内容ID
     */
    void deleteContent(Long contentId);
    
    /**
     * 获取系统统计
     * @return Object 系统统计数据
     */
    Object getStatistics();
}