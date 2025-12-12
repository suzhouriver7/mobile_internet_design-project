package dev.campuscompanionbackend.repository;

import dev.campuscompanionbackend.entity.User;
import dev.campuscompanionbackend.entity.Order;
import dev.campuscompanionbackend.entity.OrderApply;
import dev.campuscompanionbackend.enums.ApplyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


/**
 * 订单申请记录表 Repository
 */
@Repository
public interface OrderApplyRepository extends JpaRepository<OrderApply, Long> {

    /**
     * 根据用户和订单查询申请
     *
     * @param order the order
     * @param user  the user
     * @return the optional
     */
    Optional<OrderApply> findByOrderAndUser(Order order, User user);

    /**
     * 根据订单查询所有申请记录
     *
     * @param order the order
     * @return the list
     */
    List<OrderApply> findByOrder(Order order);

    /**
     * 根据用户查询所有申请记录
     *
     * @param user the user
     * @return the list
     */
    List<OrderApply> findByUser(User user);

    /**
     * 根据订单和申请状态查询所有申请记录
     *
     * @param order  the order
     * @param status the status
     * @return the list
     */
    List<OrderApply> findByOrderAndStatus(Order order, ApplyStatus status);

    /**
     * 检查用户是否已申请某订单
     *
     * @param order the order
     * @param user  the user
     * @return the boolean
     */
    boolean existsByOrderAndUser(Order order, User user);
}
