package dev.campuscompanionbackend.entity;

import dev.campuscompanionbackend.enums.ApplyStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * The type Order apply.
 */
@Data
@Entity
@Table(name = "order_apply",
        uniqueConstraints = @UniqueConstraint(columnNames = {"oid", "uid"}) // 唯一约束：一个用户只能申请一个订单一次
)
public class OrderApply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long apid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oid", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplyStatus status = ApplyStatus.PENDING_REVIEW; // 申请状态，默认待审核

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
