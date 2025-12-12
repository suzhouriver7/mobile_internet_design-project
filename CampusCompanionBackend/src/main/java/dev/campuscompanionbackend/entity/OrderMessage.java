package dev.campuscompanionbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "order_messages")
public class OrderMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oid", nullable = false)
    private Order order; // 关联的订单

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_uid", nullable = false)
    private User sender; // 发送者

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_uid", nullable = false)
    private User receiver; // 接收者

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reference_mid")
    private OrderMessage referenceMessage; // 回复的消息

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 消息内容

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 发送时间
}