package dev.campuscompanionbackend.repository;

import dev.campuscompanionbackend.entity.Order;
import dev.campuscompanionbackend.entity.User;
import dev.campuscompanionbackend.enums.ActivityType;
import dev.campuscompanionbackend.enums.Campus;
import dev.campuscompanionbackend.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单主表 Repository
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 根据发布者查询订单
     *
     * @param user the user
     * @return the list
     */
    List<Order> findByUser(User user);

    /**
     * 查询某状态的全部订单
     *
     * @param status the status
     * @return the list
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * 根据校区、活动类型和状态查询订单
     *
     * @param campus       the campus
     * @param activityType the activity type
     * @param status       the status
     * @return the list
     */
    List<Order> findByCampusAndActivityTypeAndStatus(Campus campus, ActivityType activityType, OrderStatus status);

    /**
     * 根据活动开始时间范围查询订单
     *
     * @param startTime the start time
     * @param endTime   the end time
     * @return the list
     */
    List<Order> findByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据状态查询在某时刻之前创建的订单
     *
     * @param status     the status
     * @param expiredTime the expired time
     * @return the list
     */
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt < :expiredTime")
    List<Order> findExpiredOrders(@Param("status") OrderStatus status, @Param("expiredTime") LocalDateTime expiredTime);

    /**
     * 根据发布者和状态查询订单
     *
     * @param user   the user
     * @param status the status
     * @return the list
     */
    List<Order> findByUserAndStatus(User user, OrderStatus status);
}
