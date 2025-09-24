package com.aichat.roleplay.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 创建聊天会话请求DTO
 * 遵循SOLID原则中的单一职责原则
 */
public class CreateChatRequest {

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    /**
     * 聊天标题（可选）
     */
    private String title;

    // Getters and Setters
    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}