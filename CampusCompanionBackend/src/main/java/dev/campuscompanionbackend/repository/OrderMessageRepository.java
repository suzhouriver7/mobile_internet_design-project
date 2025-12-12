package dev.campuscompanionbackend.repository;

import dev.campuscompanionbackend.entity.Order;
import dev.campuscompanionbackend.entity.OrderMessage;
import dev.campuscompanionbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 订单消息表 Repository
 */
@Repository
public interface OrderMessageRepository extends JpaRepository<OrderMessage, Long> {

    /**
     * 根据订单查询所有消息，按创建时间升序排列
     *
     * @param order the order
     * @return the list
     */
    List<OrderMessage> findByOrderOrderByCreatedAtAsc(Order order);

    /**
     * 根据发送者、接收者和订单查询消息，按创建时间升序排列
     *
     * @param sender   the sender
     * @param receiver the receiver
     * @param order    the order
     * @return the list
     */
    List<OrderMessage> findBySenderAndReceiverAndOrderOrderByCreatedAtAsc(User sender, User receiver, Order order);

    /**
     * 根据被引消息查询所有引用消息
     *
     * @param referenceMessage the reference mid
     * @return the list
     */
    List<OrderMessage> findByReferenceMessage(OrderMessage referenceMessage);
}
