package com.aichat.roleplay.service;

import java.util.List;
import java.util.Map;

/**
 * AI聊天服务接口
 * 定义AI聊天相关的业务方法
 * 支持角色扮演和流式响应
 */
public interface IAiChatService {

    void generateStreamResponseDirect(String fullPrompt, StreamResponseCallback callback);

    /**
     * 流式响应回调接口
     */
    interface StreamResponseCallback {
        void onResponse(String token);
    }
}