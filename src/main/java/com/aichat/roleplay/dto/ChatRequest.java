package com.aichat.roleplay.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 聊天请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户查询文本
     */
    private String query;
    
    /**
     * 对话上下文
     */
    private String context;
}