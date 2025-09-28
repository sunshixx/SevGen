package com.aichat.roleplay.service;

import com.aichat.roleplay.dto.RoleSelectionResult;
import com.aichat.roleplay.model.Role;

import java.util.List;

/**
 * 角色选择器接口
 * 使用LLM智能选择最适合的角色参与聊天室对话
 */
public interface IRoleSelector {
    
    /**
     * 根据用户消息和聊天室角色选择最适合的top-k个角色
     * 
     * @param userMessage 用户消息
     * @param availableRoles 聊天室中可用的角色列表
     * @param topK 选择的角色数量
     * @param context 上下文信息（可选）
     * @return 角色选择结果
     */
    RoleSelectionResult selectTopKRoles(String userMessage, List<Role> availableRoles, int topK, String context);

}