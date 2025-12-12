package dev.campuscompanionbackend.entity;

import dev.campuscompanionbackend.enums.ContentStatus;
import dev.campuscompanionbackend.enums.PostType;
import dev.campuscompanionbackend.enums.MediaType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pid; // 动态/评论ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", nullable = false)
    private User user; // 发布者

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_pid")
    private Post parentPost; // 父级动态/评论（评论关联动态、子评论关联父评论）

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oid")
    private Order order; // 关联的订单（可选）

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType type; // 内容类型：POST/COMMENT

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 内容正文

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType hasMedia = MediaType.TEXT_ONLY; // 媒体类型，默认纯文本

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentStatus status = ContentStatus.NORMAL; // 内容状态，默认正常

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
