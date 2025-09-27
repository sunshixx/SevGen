package com.aichat.roleplay.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天室实体类
 * 专门处理多agent聊天室功能
 * 遵循SOLID原则中的单一职责原则
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("chatroom")
public class ChatRoom implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 聊天会话ID，主键
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
     * 聊天室ID，多个角色可以属于同一个聊天室
     */
    @TableField("chat_room_id")
    private Long chatRoomId;

    /**
     * 聊天会话标题
     */
    @TableField("title")
    private String title;

    /**
     * 聊天室描述
     */
    @TableField("description")
    private String description;

    /**
     * 加入顺序
     */
    @TableField("join_order")
    private Integer joinOrder;

    /**
     * 会话状态：1-活跃，0-非活跃
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
     * 逻辑删除标志：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * 构造函数
     */
    public ChatRoom(Long userId, Long roleId, Long chatRoomId, String title, String description, Integer joinOrder) {
        this.userId = userId;
        this.roleId = roleId;
        this.chatRoomId = chatRoomId;
        this.title = title;
        this.description = description;
        this.joinOrder = joinOrder;
        this.isActive = true;
        this.deleted = 0;
    }
}