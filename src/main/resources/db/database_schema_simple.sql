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