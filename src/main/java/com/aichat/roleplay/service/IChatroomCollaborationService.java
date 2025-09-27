package com.aichat.roleplay.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.Map;

/**
 * 聊天室协作服务接口
 */
public interface IChatroomCollaborationService {
    
    /**
     * 处理协作消息 - SSE流式返回
     * @param chatId 聊天ID
     * @param userMessage 用户消息
     * @param context 上下文
     * @return SSE流
     */
    SseEmitter handleCollaborativeMessage(Long chatId, String userMessage, String context);
    
    /**
     * 处理协作消息 - 同步返回（用于语音场景）
     * @param chatId 聊天ID
     * @param userMessage 用户消息
     * @param context 上下文
     * @return 角色名称到响应内容的映射
     */
    Map<String, String> handleCollaborativeMessageSync(Long chatId, String userMessage, String context);
}