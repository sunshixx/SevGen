package com.aichat.roleplay.model;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天会话实体类
 * 遵循SOLID原则中的单一职责原则
 * 使用MyBatis-Plus注解进行ORM映射
 */
@TableName("chats")
public class Chat implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，自增长
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 角色ID
     */
    @TableField("role_id")
    private Long roleId;

    /**
     * 聊天标题
     */
    @TableField("title")
    private String title;

    /**
     * 是否激活
     */
    @TableField("is_active")
    private Boolean isActive;

    /**
     * 创建时间，自动填充
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间，自动填充
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标志
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    // Constructors
    public Chat() {}

    public Chat(Long userId, Long roleId, String title) {
        this.userId = userId;
        this.roleId = roleId;
        this.title = title;
        this.isActive = true;
        this.deleted = 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    // Builder pattern helper methods
    public static ChatBuilder builder() {
        return new ChatBuilder();
    }

    public static class ChatBuilder {
        private Long id;
        private Long userId;
        private Long roleId;
        private String title;
        private Boolean isActive = true;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private Integer deleted = 0;

        public ChatBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ChatBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public ChatBuilder roleId(Long roleId) {
            this.roleId = roleId;
            return this;
        }

        public ChatBuilder title(String title) {
            this.title = title;
            return this;
        }

        public ChatBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public ChatBuilder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public ChatBuilder updateTime(LocalDateTime updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public ChatBuilder deleted(Integer deleted) {
            this.deleted = deleted;
            return this;
        }

        public Chat build() {
            Chat chat = new Chat();
            chat.setId(this.id);
            chat.setUserId(this.userId);
            chat.setRoleId(this.roleId);
            chat.setTitle(this.title);
            chat.setIsActive(this.isActive);
            chat.setCreateTime(this.createTime);
            chat.setUpdateTime(this.updateTime);
            chat.setDeleted(this.deleted);
            return chat;
        }
    }
}