package dev.campuscompanionbackend.entity;

import dev.campuscompanionbackend.enums.UserStatus;
import dev.campuscompanionbackend.enums.UserType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uid;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(length = 255)
    private String avatarUrl; // 自动映射avatar_url

    @Column(length = 255)
    private String signature;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType = UserType.COMMON;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus userStatus;

    @CreationTimestamp // 自动填充创建时间
    @Column(nullable = false, updatable = false) // 创建时间不允许更新
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastLoginAt;
}
