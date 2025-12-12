package dev.campuscompanionbackend.entity;

import dev.campuscompanionbackend.enums.ActivityType;
import dev.campuscompanionbackend.enums.GenderRequire;
import dev.campuscompanionbackend.enums.Campus;
import dev.campuscompanionbackend.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long oid; // 订单唯一ID

    @ManyToOne(fetch = FetchType.LAZY) // 多对一：多个订单属于一个用户
    @JoinColumn(name = "uid", nullable = false) // 外键关联users.uid
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType activityType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GenderRequire genderRequire; // 性别要求

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Campus campus; // 校区

    @Column(nullable = false, length = 100)
    private String location; // 活动地点

    @Column(nullable = false)
    private LocalDateTime startTime; // 活动开始时间

    @Column(length = 200)
    private String note; // 备注

    @Column(nullable = false)
    private Byte maxPeople; // 最大参与人数（匹配MySQL TINYINT）

    @Column(nullable = false)
    private Byte currentPeople = 0; // 当前参与人数，默认0

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING; // 订单状态，默认待匹配

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
