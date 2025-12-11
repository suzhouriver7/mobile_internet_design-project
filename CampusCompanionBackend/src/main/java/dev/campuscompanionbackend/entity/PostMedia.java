package dev.campuscompanionbackend.entity;

import dev.campuscompanionbackend.enums.MediaType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "post_media")
public class PostMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pmid; // 媒体ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pid", nullable = false)
    private Post post; // 关联的动态/评论

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType mediaType; // 媒体类型

    @Column(nullable = false, length = 255)
    private String url; // 媒体URL

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 上传时间
}
