-- 为 messages 表添加 role_id 字段
-- 执行时间: 2025-09-25

USE `qiniuyun`;

-- 添加 role_id 字段到 messages 表
ALTER TABLE `messages` 
ADD COLUMN `role_id` BIGINT NULL COMMENT '角色ID，可以为空（兼容旧数据）' AFTER `chat_id`;

-- 添加索引提升查询性能
ALTER TABLE `messages` 
ADD KEY `idx_role_id` (`role_id`);

-- 添加外键约束（可选，根据业务需要）
-- ALTER TABLE `messages` 
-- ADD CONSTRAINT `fk_messages_role_id` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE SET NULL;

-- 验证表结构
DESCRIBE `messages`;


---增加了三列以支持前端消息显示
ALTER TABLE messages 
ADD COLUMN message_type VARCHAR(20) DEFAULT 'text',
ADD COLUMN audio_duration INT NULL,
ADD COLUMN transcribed_text TEXT NULL;