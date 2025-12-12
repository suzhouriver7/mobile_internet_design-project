package dev.campuscompanionbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "order_accept")
public class OrderAccept {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long acid; // 接受记录ID

    @OneToOne(fetch = FetchType.LAZY) // 一对一：一个订单只能被接受一次
    @JoinColumn(name = "oid", nullable = false, unique = true)
    private Order order; // 关联的订单

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepter_uid", nullable = false)
    private User accepter; // 接受订单的用户

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime acceptedAt; // 接受时间
}
