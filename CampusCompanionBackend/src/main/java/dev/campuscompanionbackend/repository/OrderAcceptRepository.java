package dev.campuscompanionbackend.repository;

import dev.campuscompanionbackend.entity.Order;
import dev.campuscompanionbackend.entity.OrderAccept;
import dev.campuscompanionbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 订单接受记录表 Repository
 */
@Repository
public interface OrderAcceptRepository extends JpaRepository<OrderAccept, Long> {

    /**
     * 根据订单查询接受记录
     *
     * @param order the order
     * @return the list
     */
    List<OrderAccept> findByOrder(Order order);

    /**
     * 根据接受者查询所有接受的订单
     *
     * @param accepter the accepter
     * @return the list
     */
    List<OrderAccept> findByAccepter(User accepter);

    /**
     * 检查订单是否已被接受
     *
     * @param order the order
     * @return the boolean
     */
    boolean existsByOrder(Order order);
}
