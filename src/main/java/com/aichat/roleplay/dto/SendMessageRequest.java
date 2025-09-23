package com.aichat.roleplay.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 发送消息请求DTO
 * 遵循SOLID原则中的单一职责原则
 */
public class SendMessageRequest {

    /**
     * 聊天会话ID
     */
    @NotNull(message = "聊天会话ID不能为空")
    private Long chatId;

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;

    /**
     * 发送者类型（user/ai）
     */
    private String senderType = "user";

    // Getters and Setters
    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }
}