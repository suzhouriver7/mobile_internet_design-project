package dev.campuscompanionbackend.entity;

import dev.campuscompanionbackend.enums.VerifyCodeRecordStatus;
import dev.campuscompanionbackend.enums.VerifyCodeRecordType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 验证码表实体类
 * 对应数据库 verify_code_record 表
 */
@Data
@Entity
@Table(name = "verify_code_record") // 明确关联数据库表名
public class VerifyCodeRecord {
    /**
     * 验证码记录唯一ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 对应 MySQL AUTO_INCREMENT
    @Column(name = "vid", nullable = false)
    private Long vid;

    /**
     * 关联的用户ID（注：字段名email，对应SQL中的email字段，语义上建议后续修改为uid）
     */
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    /**
     * 验证码
     */
    @Column(name = "code", nullable = false, length = 100)
    private String code;

    /**
     * 验证码用途
     */
    @Enumerated(EnumType.STRING) // 存储为字符串类型，对应SQL中的ENUM值
    @Column(name = "type", nullable = false)
    private VerifyCodeRecordType type;

    /**
     * 验证码状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VerifyCodeRecordStatus status;

    /**
     * 生成时间
     */
    @CreationTimestamp // 自动填充创建时间
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 过期时间
     */
    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;
}