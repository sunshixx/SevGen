package com.aichat.roleplay.service;

/**
 * AI聊天服务接口
 * 定义AI聊天相关的业务方法
 * 支持角色扮演和流式响应
 */
public interface IAiChatService {

    /**
     * 生成角色回复（同步）
     *
     * @param rolePrompt 角色提示词
     * @param userMessage 用户消息
     * @param chatHistory 聊天历史
     * @return AI回复
     */
    String generateRoleResponse(String rolePrompt, String userMessage, String chatHistory);

    /**
     * 生成角色化回复（同步）
     *
     * @param roleName 角色名称
     * @param characterPrompt 角色人格描述
     * @param userMessage 用户消息
     * @return AI回复
     */
    String generateCharacterResponse(String roleName, String characterPrompt, String userMessage);

    /**
     * 生成流式回复
     *
     * @param rolePrompt 角色提示词
     * @param userMessage 用户消息
     * @param callback 流式响应回调
     */
    void generateStreamResponse(String rolePrompt, String userMessage, StreamResponseCallback callback);

    /**
     * 直接使用完整prompt生成流式回复（用于已优化的prompt）
     *
     * @param fullPrompt 完整的prompt内容
     * @param callback 流式响应回调
     */
    void generateStreamResponseDirect(String fullPrompt, StreamResponseCallback callback);

    /**
     * 流式响应回调接口
     */
    interface StreamResponseCallback {
        void onResponse(String token);
    }
}