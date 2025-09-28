-- ===========================================
-- 角色扮演AI系统 - 数据库表结构脚本
-- 支持Spring Boot 3.2 + MyBatis-Plus 3.5.5
-- 更新时间: 2025-09-23
-- ===========================================

-- 创建数据库
CREATE
DATABASE IF NOT EXISTS `qiniuyun`
DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE
`qiniuyun`;

-- 用户表 - 存储用户基本信息
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`
(
    id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username   VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    password   VARCHAR(255) NOT NULL COMMENT '加密密码',
    email      VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    avatar     VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    active     TINYINT(1) DEFAULT 1 COMMENT '激活状态',
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted    TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标志',
    INDEX      idx_active (active),
    INDEX      idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- 角色表 - 存储AI角色信息
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles`
(
    id               BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    name             VARCHAR(100) NOT NULL UNIQUE COMMENT '角色名称',
    description      TEXT COMMENT '角色描述',
    character_prompt TEXT         NOT NULL COMMENT '角色人格提示词',
    avatar           VARCHAR(500) DEFAULT NULL COMMENT '角色头像URL',
    category         VARCHAR(50)  DEFAULT 'general' COMMENT '角色分类',
    is_public        TINYINT(1) DEFAULT 1 COMMENT '是否公开',
    created_at       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted          TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标志',
    INDEX            idx_category (category),
    INDEX            idx_is_public (is_public)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI角色信息表';

-- 聊天会话表 - 存储用户与AI的聊天会话
DROP TABLE IF EXISTS `chats`;
CREATE TABLE `chats`
(
    id         BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '聊天会话ID',
    user_id    BIGINT NOT NULL COMMENT '用户ID',
    role_id    BIGINT NOT NULL COMMENT '角色ID',
    title      VARCHAR(200) DEFAULT '新的对话' COMMENT '聊天标题',
    is_active  TINYINT(1) DEFAULT 1 COMMENT '会话状态',
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted    TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标志',
    INDEX      idx_user_id (user_id),
    INDEX      idx_role_id (role_id),
    INDEX      idx_user_role (user_id, role_id),
    INDEX      idx_is_active (is_active),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- 消息表 - 存储聊天消息详情
DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages`
(
    id          BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    chat_id     BIGINT NOT NULL COMMENT '聊天会话ID',
    sender_type ENUM('user', 'ai') NOT NULL COMMENT '发送者类型',
    content     TEXT   NOT NULL COMMENT '消息内容',
    audio_url   VARCHAR(500) DEFAULT NULL COMMENT '音频文件URL',
    is_read     TINYINT(1) DEFAULT 0 COMMENT '是否已读',
    sent_at     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    deleted     TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标志',
    INDEX       idx_chat_id (chat_id),
    INDEX       idx_sender_type (sender_type),
    INDEX       idx_is_read (is_read),
    INDEX       idx_sent_at (sent_at),
    INDEX       idx_chat_sender (chat_id, sender_type),
    FOREIGN KEY (chat_id) REFERENCES chats (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- ===========================================
-- 初始化数据
-- ===========================================

-- 插入默认AI角色
INSERT INTO `roles` (`name`, `description`, `character_prompt`, `avatar`, `category`, `is_public`)
VALUES ('智能助手', '通用的AI助手，可以回答各种问题并提供帮助',
        '你是一个友善、专业的AI助手。你会耐心地回答用户的问题，提供准确的信息和有用的建议。保持礼貌和专业的语调。',
        '/avatars/default-assistant.svg', 'general', 1),
       ('心理咨询师', '专业的心理健康咨询师，提供情感支持和心理建议',
        '你是一位经验丰富的心理咨询师。你善于倾听，具有同理心，能够提供专业的心理健康建议。你的回应温暖、理解且专业。',
        '/avatars/default-assistant.svg', 'health', 1),
       ('编程导师', '专业的编程教师，帮助学习各种编程知识',
        '你是一位资深的编程导师，擅长各种编程语言和技术。你能够用简单易懂的方式解释复杂的编程概念，提供实用的代码示例。',
        '/avatars/default-assistant.svg', 'education', 1),
       ('创意写作师', '富有想象力的写作指导老师',
        '你是一位充满创意的写作导师，擅长激发他人的写作灵感。你能够提供各种写作技巧、故事结构建议，并帮助完善文字表达。',
        '/avatars/default-assistant.svg', 'entertainment', 1),
       ('商务顾问', '专业的商业分析师和战略顾问',
        '你是一位经验丰富的商务顾问，具有敏锐的商业洞察力。你能够分析市场趋势，提供战略建议，帮助企业做出明智的商业决策。',
        '/avatars/default-assistant.svg', 'business', 1);

-- 插入测试用户 (密码: password，已使用BCrypt加密)
INSERT INTO `users` (`username`, `password`, `email`, `active`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'admin@example.com', 1),
       ('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'test@example.com', 1);

-- ===========================================
-- 使用说明
-- ===========================================
-- 1. 确保MySQL 8.0+服务已启动
-- 2. 执行此脚本创建数据库和表结构  
-- 3. 配置application.properties中的数据库连接信息
-- 4. 启动Spring Boot应用程序
-- 5. 默认管理员账号：admin / password
-- ===========================================

INSERT INTO `roles` (`name`, `description`, `character_prompt`, `avatar`, `category`, `is_public`)
VALUES (
           '爱因斯坦',
           '20世纪最伟大的物理学家之一，相对论的创立者，诺贝尔物理学奖获得者',
           '你是阿尔伯特·爱因斯坦，著名的理论物理学家。你充满好奇心和想象力，相信"想象力比知识更重要"。你用简单易懂的方式解释复杂的科学概念，善于用比喻和思想实验来说明问题。你不仅是科学家，也是人道主义者，关心社会问题和世界和平。你会用温和而幽默的语调交流，偶尔引用一些你的名言，如"上帝不掷骰子"、"好奇心比知识更重要"等。',
           '/avatars/einstein.svg',
           'education',
           1
       );

