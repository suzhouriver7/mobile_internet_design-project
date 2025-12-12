package dev.campuscompanionbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "post_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"pid", "uid"}) // 唯一约束：一个用户只能给一个内容点赞一次
)
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long plid; // 点赞记录ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pid", nullable = false)
    private Post post; // 关联的动态/评论

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", nullable = false)
    private User user; // 点赞的用户

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 点赞时间
}
