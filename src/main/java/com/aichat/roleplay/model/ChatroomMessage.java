package com.aichat.roleplay.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天室消息实体类
 * 用于存储聊天室中的用户消息和AI回复
 */
@Data
@TableName("chatroom_messages")
public class ChatroomMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，自增长
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 聊天室ID
     */
    @TableField("chat_room_id")
    private Long chatRoomId;

    /**
     * 角色ID（AI消息时使用）
     */
    @TableField("role_id")
    private Long roleId;

    /**
     * 用户ID（用户消息时使用）
     */
    @TableField("user_id")
    private Long userId;

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
     * 消息类型：text（文本）、voice（语音）、image（图片）
     */
    @TableField("message_type")
    private String messageType = "text";

    /**
     * 音频文件URL
     */
    @TableField("audio_url")
    private String audioUrl;

    /**
     * 音频时长（秒）
     */
    @TableField("audio_duration")
    private Integer audioDuration;

    /**
     * 语音转文字内容
     */
    @TableField("transcribed_text")
    private String transcribedText;

    /**
     * 发送时间
     */
    @TableField(value = "sent_at", fill = FieldFill.INSERT)
    private LocalDateTime sentAt;

    /**
     * 是否已读
     */
    @TableField("is_read")
    private Boolean isRead = false;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted = 0;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public ChatroomMessage() {}

    public ChatroomMessage(Long chatRoomId, Long roleId, Long userId, String senderType, String content) {
        this.chatRoomId = chatRoomId;
        this.roleId = roleId;
        this.userId = userId;
        this.senderType = senderType;
        this.content = content;
        this.isRead = false;
        this.deleted = 0;
    }

    /**
     * 静态构建器方法
     */
    public static ChatroomMessageBuilder builder() {
        return new ChatroomMessageBuilder();
    }

    public static class ChatroomMessageBuilder {
        private Long id;
        private Long chatRoomId;
        private Long roleId;
        private Long userId;
        private String senderType;
        private String content;
        private String messageType = "text";
        private String audioUrl;
        private Integer audioDuration;
        private String transcribedText;
        private LocalDateTime sentAt;
        private Boolean isRead = false;
        private Integer deleted = 0;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;

        public ChatroomMessageBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ChatroomMessageBuilder chatRoomId(Long chatRoomId) {
            this.chatRoomId = chatRoomId;
            return this;
        }

        public ChatroomMessageBuilder roleId(Long roleId) {
            this.roleId = roleId;
            return this;
        }

        public ChatroomMessageBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public ChatroomMessageBuilder senderType(String senderType) {
            this.senderType = senderType;
            return this;
        }

        public ChatroomMessageBuilder content(String content) {
            this.content = content;
            return this;
        }

        public ChatroomMessageBuilder messageType(String messageType) {
            this.messageType = messageType;
            return this;
        }

        public ChatroomMessageBuilder audioUrl(String audioUrl) {
            this.audioUrl = audioUrl;
            return this;
        }

        public ChatroomMessageBuilder audioDuration(Integer audioDuration) {
            this.audioDuration = audioDuration;
            return this;
        }

        public ChatroomMessageBuilder transcribedText(String transcribedText) {
            this.transcribedText = transcribedText;
            return this;
        }

        public ChatroomMessageBuilder sentAt(LocalDateTime sentAt) {
            this.sentAt = sentAt;
            return this;
        }

        public ChatroomMessageBuilder isRead(Boolean isRead) {
            this.isRead = isRead;
            return this;
        }

        public ChatroomMessageBuilder deleted(Integer deleted) {
            this.deleted = deleted;
            return this;
        }

        public ChatroomMessageBuilder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public ChatroomMessageBuilder updateTime(LocalDateTime updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public ChatroomMessage build() {
            ChatroomMessage message = new ChatroomMessage();
            message.id = this.id;
            message.chatRoomId = this.chatRoomId;
            message.roleId = this.roleId;
            message.userId = this.userId;
            message.senderType = this.senderType;
            message.content = this.content;
            message.messageType = this.messageType;
            message.audioUrl = this.audioUrl;
            message.audioDuration = this.audioDuration;
            message.transcribedText = this.transcribedText;
            message.sentAt = this.sentAt;
            message.isRead = this.isRead;
            message.deleted = this.deleted;
            message.createTime = this.createTime;
            message.updateTime = this.updateTime;
            return message;
        }
    }
}