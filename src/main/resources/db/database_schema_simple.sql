-- ===========================================
-- 角色扮演AI系统 - 开发环境快速建表脚本
-- 适用于快速开发和测试
-- ===========================================

CREATE
DATABASE IF NOT EXISTS `qiniuyun`
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE
`qiniuyun`;

-- 用户表
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`
(
    `id`         BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username`   VARCHAR(50)  NOT NULL UNIQUE,
    `password`   VARCHAR(255) NOT NULL,
    `email`      VARCHAR(100) NOT NULL UNIQUE,
    `avatar`     VARCHAR(500),
    `active`     TINYINT(1) DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`    TINYINT(1) DEFAULT 0
);

-- 角色表
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles`
(
    `id`               BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name`             VARCHAR(100) NOT NULL UNIQUE,
    `description`      TEXT,
    `character_prompt` TEXT         NOT NULL,
    `avatar`           VARCHAR(500),
    `category`         VARCHAR(50) DEFAULT 'general',
    `is_public`        TINYINT(1) DEFAULT 1,
    `created_at`       DATETIME    DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`          TINYINT(1) DEFAULT 0
);

-- 聊天会话表
DROP TABLE IF EXISTS `chats`;
CREATE TABLE `chats`
(
    `id`         BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id`    BIGINT NOT NULL,
    `role_id`    BIGINT NOT NULL,
    `title`      VARCHAR(200) DEFAULT '新的对话',
    `is_active`  TINYINT(1) DEFAULT 1,
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`    TINYINT(1) DEFAULT 0,
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
);

-- 消息表
DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages`
(
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `chat_id`     BIGINT NOT NULL,
    `sender_type` ENUM('user', 'ai') NOT NULL,
    `content`     TEXT   NOT NULL,
    `audio_url`   VARCHAR(500),
    `is_read`     TINYINT(1) DEFAULT 0,
    `sent_at`     DATETIME DEFAULT CURRENT_TIMESTAMP,
    `deleted`     TINYINT(1) DEFAULT 0,
    FOREIGN KEY (`chat_id`) REFERENCES `chats` (`id`) ON DELETE CASCADE
);



CREATE TABLE `chatroom`
(
    `id`           BIGINT AUTO_INCREMENT COMMENT '聊天会话ID，主键',
    `user_id`      BIGINT NOT NULL COMMENT '用户ID',
    `role_id`      BIGINT NOT NULL COMMENT '角色ID',
    `chat_room_id` BIGINT NOT NULL COMMENT '聊天室ID，多个角色可以属于同一个聊天室',
    `title`        VARCHAR(200) DEFAULT '新的对话' COMMENT '聊天会话标题',
    `description`  TEXT COMMENT '聊天室描述',
    `join_order`   INT COMMENT '加入顺序',
    `is_active`    TINYINT(1) DEFAULT 1 COMMENT '会话状态：1-活跃，0-非活跃',
    `created_at`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY            `idx_user_id` (`user_id`),
    KEY            `idx_role_id` (`role_id`),
    KEY            `idx_chat_room_id` (`chat_room_id`),
    KEY            `idx_user_role` (`user_id`, `role_id`),
    KEY            `idx_user_chatroom` (`user_id`, `chat_room_id`),
    KEY            `idx_is_active` (`is_active`),
    KEY            `idx_created_at` (`created_at`),
    KEY            `idx_join_order` (`join_order`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='聊天会话表 - 支持多角色聊天室';

-- 创建反思日志表
CREATE TABLE IF NOT EXISTS reflection_logs (
                                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                               chat_id BIGINT NOT NULL COMMENT '聊天会话ID',
                                               role_id BIGINT NOT NULL COMMENT '角色ID',
                                               user_id BIGINT COMMENT '用户ID',
                                               original_query TEXT NOT NULL COMMENT '原始用户输入',
                                               ai_response TEXT NOT NULL COMMENT 'AI回复内容',
                                               action_type VARCHAR(20) NOT NULL COMMENT '反思动作类型: SUCCESS, RETRY, ERROR',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    regenerated_query TEXT COMMENT '重新生成的查询',
    final_output TEXT COMMENT '最终输出',
    detected_issue VARCHAR(500) COMMENT '检测到的问题类型',
    reason_analysis TEXT COMMENT '反思原因分析',
    error_message TEXT COMMENT '错误信息',
    processing_time BIGINT COMMENT '处理耗时（毫秒）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_chat_id (chat_id),
    INDEX idx_role_id (role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_action_type (action_type),
    INDEX idx_create_time (create_time)
    ) COMMENT='反思日志表';
-- 基础索引
CREATE INDEX idx_user_active ON users (active);
CREATE INDEX idx_role_public ON roles (is_public, category);
CREATE INDEX idx_chat_user ON chats (user_id);
CREATE INDEX idx_chat_role ON chats (role_id);
CREATE INDEX idx_message_chat ON messages (chat_id);
CREATE INDEX idx_message_sent ON messages (sent_at);





-- 初始化数据
INSERT INTO `roles` (`name`, `description`, `character_prompt`, `category`)
VALUES ('智能助手', '通用AI助手', '你是一个友善、专业的AI助手，会耐心回答用户问题。', 'general'),
       ('编程导师', '编程学习助手', '你是一位编程导师，能用简单易懂的方式解释编程概念。', 'education'),
       ('创意作家', '创意写作助手', '你是一位富有想象力的写作导师，能激发创作灵感。', 'entertainment');

-- 测试用户 (密码: password)
INSERT INTO `users` (`username`, `password`, `email`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'admin@test.com'),
       ('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'test@test.com');