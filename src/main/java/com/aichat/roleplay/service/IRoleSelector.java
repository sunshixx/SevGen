package com.aichat.roleplay.service;

import com.aichat.roleplay.dto.RoleSelectionResult;

/**
 * 角色选择器接口
 * 负责根据用户输入选择合适的角色组合
 */
public interface IRoleSelector {
    
    /**
     * 根据用户输入选择角色
     *
     * @param userId 用户ID
     * @param query 用户输入
     * @param context 对话上下文
     * @return 角色选择结果
     */
    RoleSelectionResult selectRoles(Long userId, String query, String context);
}