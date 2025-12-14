package dev.campuscompanionbackend.controller;

import dev.campuscompanionbackend.dto.response.ApiResponse;
import dev.campuscompanionbackend.enums.OrderStatus;
import dev.campuscompanionbackend.enums.UserType;
import dev.campuscompanionbackend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员功能相关接口
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController extends BaseController {
    
    private final AdminService adminService;
    
    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }
    
    /**
     * 获取用户列表（管理员）
     * @param page 页码，默认1
     * @param size 每页数量，默认20
     * @param userType 用户类型
     * @return ApiResponse<Object>
     */
    @GetMapping("/users")
    public ApiResponse<Object> getUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) UserType userType) {
        Object result = adminService.getUsers(page, size, userType);
        return success(result);
    }
    
    /**
     * 修改用户权限
     * @param userId 用户ID
     * @param userType 用户类型
     * @return ApiResponse<Void>
     */
    @PutMapping("/users/{userId}/type")
    public ApiResponse<Void> updateUserType(
            @PathVariable Long userId,
            @RequestParam UserType userType) {
        adminService.updateUserType(userId, userType);
        return success("修改成功", null);
    }
    
    /**
     * 管理订单
     * @param orderId 订单ID
     * @param status 订单状态
     * @return ApiResponse<Void>
     */
    @PutMapping("/orders/{orderId}")
    public ApiResponse<Void> manageOrder(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        adminService.manageOrder(orderId, status);
        return success("修改成功", null);
    }
    
    /**
     * 删除任意内容
     * @param contentId 内容ID
     * @return ApiResponse<Void>
     */
    @DeleteMapping("/contents/{contentId}")
    public ApiResponse<Void> deleteContent(@PathVariable Long contentId) {
        adminService.deleteContent(contentId);
        return success("删除成功", null);
    }
    
    /**
     * 获取系统统计
     * @return ApiResponse<Object>
     */
    @GetMapping("/statistics")
    public ApiResponse<Object> getStatistics() {
        Object statistics = adminService.getStatistics();
        return success(statistics);
    }
}