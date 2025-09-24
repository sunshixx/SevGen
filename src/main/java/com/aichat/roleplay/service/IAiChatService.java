package com.aichat.roleplay.service;

/**
 * AI聊天服务接口
 */
public interface IAiChatService {

    /**
     * 生成AI角色回复
     *
     * @param rolePrompt  角色人格提示词
     * @param userMessage 用户消息
     * @param chatHistory 聊天历史（可选）
     * @return AI回复内容
     */
    String generateRoleResponse(String rolePrompt, String userMessage, String chatHistory);

    /**
     * 生成角色化的回复
     *
     * @param roleName        角色名称
     * @param characterPrompt 角色人格描述
     * @param userMessage     用户消息
     * @return 角色化回复
     */
    String generateCharacterResponse(String roleName, String characterPrompt, String userMessage);

    /**
     * 流式生成AI回复（用于SSE）
     *
     * @param rolePrompt  角色人格提示词
     * @param userMessage 用户消息
     * @param callback    回调函数处理流式响应
     */
    void generateStreamResponse(String rolePrompt, String userMessage, StreamResponseCallback callback);

    /**
     * 流式响应回调接口
     */
    @FunctionalInterface
    interface StreamResponseCallback {
        void onResponse(String chunk);
    }
}