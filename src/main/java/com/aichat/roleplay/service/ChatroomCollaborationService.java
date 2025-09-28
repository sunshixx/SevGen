package com.aichat.roleplay.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ChatroomCollaborationService {
    
    /**
     * 处理协作消息（流式响应）
     */
    SseEmitter handleCollaborativeMessage(Long chatRoomId, String userMessage, String context);
}
