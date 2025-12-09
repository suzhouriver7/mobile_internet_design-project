-- 创建数据库
create schema `internet-project`;
use `internet-project`;


-- 用户表
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    avatar_url VARCHAR(255),
    signature VARCHAR(255),
    user_type TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE (email)
);

-- 预约订单主表
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    activity_type TINYINT NOT NULL,
    tags1 TINYINT,
    tags2 TINYINT,
    tags3 TINYINT,
    area TINYINT NOT NULL,
    location VARCHAR(100),
    start_time DATETIME NOT NULL,
    note VARCHAR(200),
    max_people TINYINT NOT NULL,
    current_people TINYINT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 用户申请加入订单表
CREATE TABLE order_apply (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,  -- 0=待审核，1=已通过，2=拒绝
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (order_id, user_id),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 订单交流问询表
CREATE TABLE order_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
);

-- 订单接受记录表
CREATE TABLE order_accept (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    accepter_id BIGINT NOT NULL,
    accepted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (accepter_id) REFERENCES users(id)
);

-- 动态/评论主表
CREATE TABLE content_nodes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    parent_id BIGINT DEFAULT NULL,
    order_id BIGINT DEFAULT NULL,
    type TINYINT NOT NULL,  -- 0=动态，1=评论
    content TEXT NOT NULL,
    media TINYINT DEFAULT 0,  -- 0=纯文本，1=包含媒体
    status TINYINT NOT NULL DEFAULT 0,  -- 0=正常，1=已删除
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (parent_id) REFERENCES content_nodes(id),
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- 动态/评论媒体表
CREATE TABLE content_media (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content_id BIGINT NOT NULL,
    media_type TINYINT NOT NULL,  -- 1=图片，2=视频
    url VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (content_id) REFERENCES content_nodes(id)
);

-- 动态/评论点赞表
CREATE TABLE content_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(content_id, user_id),
    FOREIGN KEY (content_id) REFERENCES content_nodes(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
