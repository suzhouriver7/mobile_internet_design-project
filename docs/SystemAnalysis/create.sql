-- 设置MySQL字符集
SET PERSIST character_set_server = utf8mb4;
SET PERSIST collation_server = utf8mb4_general_ci;
SET NAMES utf8mb4;

-- DROP DATABASE 会彻底删除数据库及所有表/数据，执行前请确认！
-- DROP DATABASE IF EXISTS campus_companion;

-- 指定utf8mb4字符集，兼容所有字符表情
CREATE DATABASE campus_companion
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE campus_companion;

-- 用户表 users
CREATE TABLE IF NOT EXISTS users
(
    uid           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户唯一ID',
    email         VARCHAR(100) UNIQUE                                 NOT NULL COMMENT '用户邮箱（唯一）',
    password      VARCHAR(255)                                        NOT NULL COMMENT '加密后的密码',
    nickname      VARCHAR(50)                                         NOT NULL COMMENT '用户昵称',
    avatar_url    VARCHAR(255) COMMENT '头像URL',
    signature     VARCHAR(255) COMMENT '个性签名',
    user_type     ENUM ('COMMON', 'ADMIN')                            NOT NULL DEFAULT 'COMMON' COMMENT '用户类型：COMMON-普通用户，ADMIN-管理员',
    user_status   ENUM ('ONLINE', 'OFFLINE', 'REGISTERING', 'BANNED') NOT NULL COMMENT '用户状态：ONLINE-在线，OFFLINE-离线，REGISTERING-注册中，BANNED-封禁中',
    created_at    DATETIME                                            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    last_login_at DATETIME                                            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上次登录时间'
) COMMENT = '用户信息表';

-- 订单主表 orders
CREATE TABLE IF NOT EXISTS orders
(
    oid            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单唯一ID',
    uid            BIGINT                                                                                 NOT NULL COMMENT '发布订单的用户ID',
    activity_type  ENUM ('BASKETBALL', 'BADMINTON', 'MEAL', 'STUDY', 'MOVIE', 'RUNNING', 'GAME', 'OTHER') NOT NULL COMMENT '活动类型',
    gender_require ENUM ('MALE', 'FEMALE', 'ANY')                                                         NOT NULL COMMENT '性别要求',
    campus         ENUM ('LIANGXIANG', 'ZHONGGUANCUN', 'ZHUHAI', 'XISHAN', 'OTHER_CAMPUS')                NOT NULL COMMENT '校区',
    location       VARCHAR(100)                                                                           NOT NULL COMMENT '活动地点',
    start_time     DATETIME                                                                               NOT NULL COMMENT '活动开始时间',
    note           VARCHAR(200) COMMENT '备注信息',
    max_people     TINYINT                                                                                NOT NULL COMMENT '最大参与人数',
    current_people TINYINT                                                                                NOT NULL DEFAULT 0 COMMENT '当前参与人数',
    status         ENUM ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'EXPIRED')                   NOT NULL DEFAULT 'PENDING' COMMENT '订单状态',
    created_at     DATETIME                                                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     DATETIME                                                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (uid) REFERENCES users (uid) ON DELETE CASCADE
) COMMENT = '订单主表';

-- 订单申请表 order_apply
CREATE TABLE IF NOT EXISTS order_apply
(
    apid       BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '申请记录ID',
    oid        BIGINT                                                             NOT NULL COMMENT '关联的订单ID',
    uid        BIGINT                                                             NOT NULL COMMENT '申请的用户ID',
    status     ENUM ('PENDING_REVIEW', 'APPROVED', 'REJECTED', 'CANCELLED_APPLY') NOT NULL DEFAULT 'PENDING_REVIEW' COMMENT '申请状态',
    created_at DATETIME                                                           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    FOREIGN KEY (oid) REFERENCES orders (oid) ON DELETE CASCADE,
    FOREIGN KEY (uid) REFERENCES users (uid) ON DELETE CASCADE,
    UNIQUE KEY uk_oid_uid (oid, uid)
) COMMENT = '订单申请表';

-- 订单消息表 order_messages
CREATE TABLE IF NOT EXISTS order_messages
(
    mid           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    oid           BIGINT   NOT NULL COMMENT '关联的订单ID',
    sender_uid    BIGINT   NOT NULL COMMENT '发送者用户ID',
    receiver_uid  BIGINT   NOT NULL COMMENT '接收者用户ID',
    reference_mid BIGINT COMMENT '回复的消息ID（可选）',
    content       TEXT     NOT NULL COMMENT '消息内容',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    FOREIGN KEY (oid) REFERENCES orders (oid) ON DELETE CASCADE,
    FOREIGN KEY (sender_uid) REFERENCES users (uid) ON DELETE CASCADE,
    FOREIGN KEY (receiver_uid) REFERENCES users (uid) ON DELETE CASCADE,
    FOREIGN KEY (reference_mid) REFERENCES order_messages (mid) ON DELETE SET NULL
) COMMENT = '订单聊天消息表';

-- 订单接受记录表 order_accept
CREATE TABLE IF NOT EXISTS order_accept
(
    acid         BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '接受记录ID',
    oid          BIGINT   NOT NULL UNIQUE COMMENT '关联的订单ID（一个订单只能被接受一次）',
    accepter_uid BIGINT   NOT NULL COMMENT '接受订单的用户ID',
    accepted_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '接受时间',
    FOREIGN KEY (oid) REFERENCES orders (oid) ON DELETE CASCADE,
    FOREIGN KEY (accepter_uid) REFERENCES users (uid) ON DELETE CASCADE
) COMMENT = '订单接受记录表';

-- 动态/评论主表 posts
CREATE TABLE IF NOT EXISTS posts
(
    pid        BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '动态/评论ID',
    uid        BIGINT                                            NOT NULL COMMENT '发布者用户ID',
    parent_pid BIGINT COMMENT '父级ID（评论关联动态/子评论关联父评论）',
    oid        BIGINT COMMENT '关联的订单ID（可选）',
    type       ENUM ('POST', 'COMMENT')                          NOT NULL COMMENT '内容类型：POST-动态，COMMENT-评论',
    content    TEXT                                              NOT NULL COMMENT '内容正文',
    has_media  ENUM ('TEXT_ONLY', 'IMAGE', 'VIDEO')              NOT NULL DEFAULT 'TEXT_ONLY' COMMENT '媒体类型：TEXT_ONLY-纯文本，IMAGE-图片，VIDEO-视频',
    status     ENUM ('NORMAL', 'DELETED', 'PENDING', 'REJECTED') NOT NULL DEFAULT 'NORMAL' COMMENT '内容状态',
    created_at DATETIME                                          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    updated_at DATETIME                                          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (uid) REFERENCES users (uid) ON DELETE CASCADE,
    FOREIGN KEY (parent_pid) REFERENCES posts (pid) ON DELETE CASCADE,
    FOREIGN KEY (oid) REFERENCES orders (oid) ON DELETE SET NULL
) COMMENT = '动态/评论主表';

-- 动态/评论媒体表 post_media
CREATE TABLE IF NOT EXISTS post_media
(
    pmid       BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '媒体ID',
    pid        BIGINT                               NOT NULL COMMENT '关联的动态/评论ID',
    media_type ENUM ('TEXT_ONLY', 'IMAGE', 'VIDEO') NOT NULL COMMENT '媒体类型',
    url        VARCHAR(255)                         NOT NULL COMMENT '媒体文件URL',
    created_at DATETIME                             NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    FOREIGN KEY (pid) REFERENCES posts (pid) ON DELETE CASCADE
) COMMENT = '动态/评论媒体附件表';

-- 动态/评论点赞表 post_likes
CREATE TABLE IF NOT EXISTS post_likes
(
    plid       BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '点赞记录ID',
    pid        BIGINT   NOT NULL COMMENT '关联的动态/评论ID',
    uid        BIGINT   NOT NULL COMMENT '点赞的用户ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    FOREIGN KEY (pid) REFERENCES posts (pid) ON DELETE CASCADE,
    FOREIGN KEY (uid) REFERENCES users (uid) ON DELETE CASCADE,
    UNIQUE KEY uk_pid_uid (pid, uid)
) COMMENT = '动态/评论点赞表';