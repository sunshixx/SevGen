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