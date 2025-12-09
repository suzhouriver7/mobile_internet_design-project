-- 创建数据库
create schema `internet-project`;
use `internet-project`;


-- 用户表
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户唯一编号',
    email VARCHAR(100) NOT NULL COMMENT '邮箱（登录与找回密码）',
    password_hash VARCHAR(255) NOT NULL COMMENT '加密后的密码',
    nickname VARCHAR(50) COMMENT '用户昵称',
    avatar_url VARCHAR(255) COMMENT '头像 URL',
    signature VARCHAR(255) COMMENT '个性签名',
    user_type TINYINT NOT NULL DEFAULT 0 COMMENT '用户类型 0普通用户 1管理员',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE(email)
) COMMENT='用户表';

-- 预约订单主表
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单唯一 ID',
    user_id BIGINT NOT NULL COMMENT '创建订单的用户 ID',
    activity_type TINYINT NOT NULL COMMENT '活动类型 0篮球 1羽毛球 2吃饭 等等',
    tags1 TINYINT COMMENT '性别要求 0男 1女 2不限',
    tags2 TINYINT COMMENT '扩展标签2',
    tags3 TINYINT COMMENT '扩展标签3',
    area TINYINT NOT NULL COMMENT '校区 0良乡 2中关村 3珠海 等',
    location VARCHAR(100) COMMENT '具体地点',
    start_time DATETIME NOT NULL COMMENT '活动开始时间',
    note VARCHAR(200) COMMENT '备注',
    max_people TINYINT NOT NULL COMMENT '参与人数上限',
    current_people TINYINT NOT NULL DEFAULT 0 COMMENT '当前人数',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态 0未完成 1已完成 2已过期 3已删除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id)
) COMMENT='预约订单主表';


-- 用户申请加入订单表
CREATE TABLE order_apply (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '申请记录 ID',
    order_id BIGINT NOT NULL COMMENT '申请加入的订单',
    user_id BIGINT NOT NULL COMMENT '申请人',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0待审核 1通过 2拒绝',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    UNIQUE(order_id, user_id),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
) COMMENT='订单申请表';


-- 订单交流问询表
CREATE TABLE order_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '交流消息 ID',
    order_id BIGINT NOT NULL COMMENT '订单 ID',
    sender_id BIGINT NOT NULL COMMENT '发送者 ID',
    receiver_id BIGINT NOT NULL COMMENT '接收者 ID',
    content TEXT NOT NULL COMMENT '消息内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
) COMMENT='订单问询消息表';


-- 订单接受记录表
CREATE TABLE order_accept (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '接受记录 ID',
    order_id BIGINT NOT NULL UNIQUE COMMENT '订单 ID（唯一）',
    accepter_id BIGINT NOT NULL COMMENT '接受订单的用户',
    accepted_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '接受时间',
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (accepter_id) REFERENCES users(id)
) COMMENT='订单接受记录表';


-- 动态/评论主表
CREATE TABLE content_nodes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '内容唯一 ID（动态或评论）',
    user_id BIGINT NOT NULL COMMENT '发布者',
    parent_id BIGINT DEFAULT NULL COMMENT '父节点(动态=NULL，评论=父内容ID)',
    order_id BIGINT DEFAULT NULL COMMENT '引用的订单 ID（可空）',
    type TINYINT NOT NULL COMMENT '0动态 1评论',
    content TEXT NOT NULL COMMENT '文本内容',
    media TINYINT NOT NULL DEFAULT 0 COMMENT '0纯文本 1含媒体',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (parent_id) REFERENCES content_nodes(id),
    FOREIGN KEY (order_id) REFERENCES orders(id)
) COMMENT='动态与评论主表';


-- 动态/评论媒体表
CREATE TABLE content_media (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '媒体 ID',
    content_id BIGINT NOT NULL COMMENT '所属内容节点',
    media_type TINYINT NOT NULL COMMENT '1图片 2视频',
    url VARCHAR(255) NOT NULL COMMENT '文件 URL',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    FOREIGN KEY (content_id) REFERENCES content_nodes(id)
) COMMENT='动态/评论媒体表';


-- 动态/评论点赞表
CREATE TABLE content_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '点赞记录 ID',
    content_id BIGINT NOT NULL COMMENT '点赞的内容 ID',
    user_id BIGINT NOT NULL COMMENT '点赞用户',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    UNIQUE(content_id, user_id),
    FOREIGN KEY (content_id) REFERENCES content_nodes(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
) COMMENT='动态/评论点赞表';

