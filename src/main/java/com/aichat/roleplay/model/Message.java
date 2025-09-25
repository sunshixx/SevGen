package com.aichat.roleplay.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息实体类
 * 遵循SOLID原则中的单一职责原则
 * 使用MyBatis-Plus注解进行ORM映射
 */
@Data
@TableName("messages")
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，自增长
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 聊天会话ID
     */
    @TableField("chat_id")
    private Long chatId;

    @TableField("role_id")
    private Long roleId;

    /**
     * 发送者类型：user 或 ai
     */
    @TableField("sender_type")
    private String senderType;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * 音频文件URL
     */
    @TableField("audio_url")
    private String audioUrl;

    /**
     * 是否已读
     */
    @TableField("is_read")
    private Boolean isRead;

    /**
     * 发送时间，自动填充
     */
    @TableField(value = "sent_at", fill = FieldFill.INSERT)
    private LocalDateTime sentAt;

    /**
     * 逻辑删除标志
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    // Constructors
    public Message() {}

    public Message(Long chatId, Long roleId,String senderType, String content) {
        this.chatId = chatId;
        this.roleId = roleId;
        this.senderType = senderType;
        this.content = content;
        this.isRead = false;
        this.deleted = 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    // Builder pattern helper methods
    public static MessageBuilder builder() {
        return new MessageBuilder();
    }

    public static class MessageBuilder {
        private Long id;
        private Long chatId;
        private String senderType;
        private String content;
        private String audioUrl;
        private Boolean isRead = false;
        private LocalDateTime sentAt;
        private Integer deleted = 0;

        public MessageBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public MessageBuilder chatId(Long chatId) {
            this.chatId = chatId;
            return this;
        }

        public MessageBuilder senderType(String senderType) {
            this.senderType = senderType;
            return this;
        }

        public MessageBuilder content(String content) {
            this.content = content;
            return this;
        }

        public MessageBuilder audioUrl(String audioUrl) {
            this.audioUrl = audioUrl;
            return this;
        }

        public MessageBuilder isRead(Boolean isRead) {
            this.isRead = isRead;
            return this;
        }

        public MessageBuilder sentAt(LocalDateTime sentAt) {
            this.sentAt = sentAt;
            return this;
        }

        public MessageBuilder deleted(Integer deleted) {
            this.deleted = deleted;
            return this;
        }

        public Message build() {
            Message message = new Message();
            message.setId(this.id);
            message.setChatId(this.chatId);
            message.setSenderType(this.senderType);
            message.setContent(this.content);
            message.setAudioUrl(this.audioUrl);
            message.setIsRead(this.isRead);
            message.setSentAt(this.sentAt);
            message.setDeleted(this.deleted);
            return message;
        }
    }
}