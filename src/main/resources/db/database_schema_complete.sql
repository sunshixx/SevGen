-- ===========================================
-- 角色扮演AI系统数据库设计
-- 支持Spring Boot 3.2 + MyBatis-Plus 3.5.5 + LangChain4j 0.34.0
-- 创建时间: 2025-09-23
-- 数据库版本: MySQL 8.0+
-- ===========================================

-- 创建数据库
CREATE
DATABASE IF NOT EXISTS `qiniuyun`
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE
`qiniuyun`;

-- ===========================================
-- 1. 用户表 (users)
-- 存储用户基本信息和账户状态
-- ===========================================
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`
(
    `id`         BIGINT AUTO_INCREMENT COMMENT '用户ID，主键',
    `username`   VARCHAR(50)  NOT NULL COMMENT '用户名，唯一标识',
    `password`   VARCHAR(255) NOT NULL COMMENT '加密后的密码',
    `email`      VARCHAR(100) NOT NULL COMMENT '邮箱地址，用于找回密码',
    `avatar`     VARCHAR(500) DEFAULT NULL COMMENT '用户头像URL',
    `active`     TINYINT(1) DEFAULT 1 COMMENT '账户状态：1-激活，0-未激活',
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`    TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY          `idx_active` (`active`),
    KEY          `idx_created_at` (`created_at`)
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='用户信息表';

-- ===========================================
-- 2. 角色表 (roles)
-- 存储AI角色的基本信息和人格设定
-- ===========================================
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles`
(
    `id`               BIGINT AUTO_INCREMENT COMMENT '角色ID，主键',
    `name`             VARCHAR(100) NOT NULL COMMENT '角色名称',
    `description`      TEXT COMMENT '角色描述信息',
    `character_prompt` TEXT         NOT NULL COMMENT '角色人格提示词，用于AI生成回复',
    `avatar`           VARCHAR(500) DEFAULT NULL COMMENT '角色头像URL',
    `category`         VARCHAR(50)  DEFAULT 'general' COMMENT '角色分类：general-通用，entertainment-娱乐，education-教育，business-商务等',
    `is_public`        TINYINT(1) DEFAULT 1 COMMENT '是否公开：1-公开，0-私有',
    `created_at`       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`          TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY                `idx_category` (`category`),
    KEY                `idx_is_public` (`is_public`),
    KEY                `idx_created_at` (`created_at`)
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='AI角色信息表';

-- ===========================================
-- 3. 聊天会话表 (chats)
-- 存储用户与AI角色的聊天会话信息
-- ===========================================
DROP TABLE IF EXISTS `chats`;
CREATE TABLE `chats`
(
    `id`         BIGINT AUTO_INCREMENT COMMENT '聊天会话ID，主键',
    `user_id`    BIGINT NOT NULL COMMENT '用户ID，外键关联users表',
    `role_id`    BIGINT NOT NULL COMMENT '角色ID，外键关联roles表',
    `title`      VARCHAR(200) DEFAULT '新的对话' COMMENT '聊天会话标题',
    `is_active`  TINYINT(1) DEFAULT 1 COMMENT '会话状态：1-活跃，0-非活跃',
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`    TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY          `idx_user_id` (`user_id`),
    KEY          `idx_role_id` (`role_id`),
    KEY          `idx_user_role` (`user_id`, `role_id`),
    KEY          `idx_is_active` (`is_active`),
    KEY          `idx_created_at` (`created_at`),
    CONSTRAINT `fk_chats_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_chats_role_id` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='聊天会话表';

-- ===========================================
-- 4. 消息表 (messages)
-- 存储聊天会话中的具体消息内容
-- ===========================================
DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages`
(
    `id`          BIGINT AUTO_INCREMENT COMMENT '消息ID，主键',
    `chat_id`     BIGINT NOT NULL COMMENT '聊天会话ID，外键关联chats表',
    `sender_type` ENUM('user', 'ai') NOT NULL COMMENT '发送者类型：user-用户，ai-AI角色',
    `content`     TEXT   NOT NULL COMMENT '消息内容',
    `audio_url`   VARCHAR(500) DEFAULT NULL COMMENT '音频文件URL（如果支持语音消息）',
    `is_read`     TINYINT(1) DEFAULT 0 COMMENT '是否已读：1-已读，0-未读',
    `sent_at`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    `deleted`     TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY           `idx_chat_id` (`chat_id`),
    KEY           `idx_sender_type` (`sender_type`),
    KEY           `idx_is_read` (`is_read`),
    KEY           `idx_sent_at` (`sent_at`),
    KEY           `idx_chat_sender` (`chat_id`, `sender_type`),
    CONSTRAINT `fk_messages_chat_id` FOREIGN KEY (`chat_id`) REFERENCES `chats` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='聊天消息表';

-- ===========================================
-- 5. 初始化数据
-- ===========================================

-- 插入预设的AI角色
INSERT INTO `roles` (`name`, `description`, `character_prompt`, `category`, `is_public`)
VALUES ('智能助手', '通用的AI助手，可以回答各种问题并提供帮助',
        '你是一个友善、专业的AI助手。你会耐心地回答用户的问题，提供准确的信息和有用的建议。保持礼貌和专业的语调。',
        'general', 1),

       ('心理咨询师', '专业的心理健康咨询师，提供情感支持和心理建议',
        '你是一位经验丰富的心理咨询师。你善于倾听，具有同理心，能够提供专业的心理健康建议。你的回应温暖、理解且专业，注重保护来访者的隐私和感受。',
        'health', 1),

       ('编程导师', '专业的编程教师，帮助学习各种编程知识',
        '你是一位资深的编程导师，擅长各种编程语言和技术。你能够用简单易懂的方式解释复杂的编程概念，提供实用的代码示例，并鼓励学生不断学习和实践。',
        'education', 1),

       ('创意写作师', '富有想象力的写作指导老师',
        '你是一位充满创意的写作导师，擅长激发他人的写作灵感。你能够提供各种写作技巧、故事结构建议，并帮助完善文字表达。你的语言富有感染力，能够激励他人发挥创造力。',
        'entertainment', 1),

       ('商务顾问', '专业的商业分析师和战略顾问',
        '你是一位经验丰富的商务顾问，具有敏锐的商业洞察力。你能够分析市场趋势，提供战略建议，帮助企业和个人做出明智的商业决策。你的回答专业、务实且具有前瞻性。',
        'business', 1);

-- 插入测试用户（可选，密码需要根据实际加密方式调整）
INSERT INTO `users` (`username`, `password`, `email`, `active`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'admin@example.com', 1),
       ('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'test@example.com', 1);

-- ===========================================
-- 6. 创建视图（可选）
-- ===========================================

-- 聊天会话详情视图
CREATE VIEW `v_chat_details` AS
SELECT c.id                                                                           AS chat_id,
       c.title                                                                        AS chat_title,
       c.is_active,
       c.created_at                                                                   AS chat_created_at,
       u.id                                                                           AS user_id,
       u.username,
       u.avatar                                                                       AS user_avatar,
       r.id                                                                           AS role_id,
       r.name                                                                         AS role_name,
       r.avatar                                                                       AS role_avatar,
       r.category                                                                     AS role_category,
       (SELECT COUNT(*) FROM messages m WHERE m.chat_id = c.id AND m.deleted = 0)     AS message_count,
       (SELECT MAX(sent_at) FROM messages m WHERE m.chat_id = c.id AND m.deleted = 0) AS last_message_time
FROM chats c
         JOIN users u ON c.user_id = u.id
         JOIN roles r ON c.role_id = r.id
WHERE c.deleted = 0
  AND u.deleted = 0
  AND r.deleted = 0;

-- 消息统计视图
CREATE VIEW `v_message_stats` AS
SELECT c.id                                               AS chat_id,
       c.title                                            AS chat_title,
       u.username,
       r.name                                             AS role_name,
       COUNT(CASE WHEN m.sender_type = 'user' THEN 1 END) AS user_message_count,
       COUNT(CASE WHEN m.sender_type = 'ai' THEN 1 END)   AS ai_message_count,
       COUNT(*)                                           AS total_message_count,
       MAX(m.sent_at)                                     AS last_message_time,
       MIN(m.sent_at)                                     AS first_message_time
FROM chats c
         JOIN users u ON c.user_id = u.id
         JOIN roles r ON c.role_id = r.id
         LEFT JOIN messages m ON c.id = m.chat_id AND m.deleted = 0
WHERE c.deleted = 0
  AND u.deleted = 0
  AND r.deleted = 0
GROUP BY c.id, c.title, u.username, r.name;

-- ===========================================
-- 7. 性能优化建议
-- ===========================================

-- 为频繁查询的字段添加复合索引
CREATE INDEX `idx_messages_chat_sent` ON `messages` (`chat_id`, `sent_at` DESC);
CREATE INDEX `idx_chats_user_updated` ON `chats` (`user_id`, `updated_at` DESC);

-- ===========================================
-- 8. 权限设置（根据实际需求调整）
-- ===========================================

-- 创建应用专用数据库用户（可选）
-- CREATE USER 'roleplay_app'@'localhost' IDENTIFIED BY 'your_secure_password';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON qiniuyun.* TO 'roleplay_app'@'localhost';
-- FLUSH PRIVILEGES;

-- ===========================================
-- 完成建表脚本
-- ===========================================

-- 显示所有表的状态
SHOW
TABLE STATUS LIKE '%';

-- 验证外键约束
SELECT TABLE_NAME,
       CONSTRAINT_NAME,
       CONSTRAINT_TYPE,
       REFERENCED_TABLE_NAME,
       REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'qiniuyun'
  AND REFERENCED_TABLE_NAME IS NOT NULL;

-- 数据库建表脚本执行完成
-- 建议在生产环境中：
-- 1. 调整字符集和排序规则
-- 2. 优化索引策略
-- 3. 配置备份策略
-- 4. 设置合适的用户权限
-- 5. 监控数据库性能