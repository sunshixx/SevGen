package com.aichat.roleplay.service;

import com.aichat.roleplay.dto.ChatRequest;
import com.aichat.roleplay.dto.ChatResponse;

/**
 * 聊天编排服务接口
 * 负责协调意图分类、角色调度和响应生成的整体流程
 */
public interface IChatOrchestrator {
    
    /**
     * 处理聊天请求
     *
     * @param request 聊天请求
     * @return 聊天响应
     */
    ChatResponse chat(ChatRequest request);
}