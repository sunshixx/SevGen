-- 创建聊天室消息表
CREATE TABLE chatroom_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    chat_room_id BIGINT NOT NULL COMMENT '聊天室ID',
    role_id BIGINT COMMENT '角色ID（AI消息时使用）',
    user_id BIGINT COMMENT '用户ID（用户消息时使用）',
    sender_type VARCHAR(20) NOT NULL COMMENT '发送者类型：user/ai',
    content TEXT NOT NULL COMMENT '消息内容',
    message_type VARCHAR(20) DEFAULT 'text' COMMENT '消息类型：text/voice/image',
    audio_url VARCHAR(500) COMMENT '音频文件URL',
    audio_duration INT COMMENT '音频时长（秒）',
    transcribed_text TEXT COMMENT '语音转文字内容',
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_chat_room_id (chat_room_id),
    INDEX idx_role_id (role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_sender_type (sender_type),
    INDEX idx_sent_at (sent_at),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天室消息表';