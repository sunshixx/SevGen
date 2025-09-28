package com.aichat.roleplay.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天室
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("chatroom")
public class ChatRoom implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID，主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 聊天室ID，同一个聊天室的多个角色共享此ID
     */
    @TableField("chat_room_id")
    private Long chatRoomId;

    /**
     * 创建者用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 角色ID
     */
    @TableField("role_id")
    private Long roleId;

    /**
     * 聊天室标题
     */
    @TableField("title")
    private String title;

    /**
     * 聊天室描述
     */
    @TableField("description")
    private String description;



    /**
     * 角色加入顺序，用于排序显示
     */
    @TableField("join_order")
    private Integer joinOrder;

    /**
     * 该角色在聊天室中是否激活：1-激活，0-暂停
     */
    @TableField("is_active")
    private Boolean isActive;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标志：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    // 构造函数
    public ChatRoom(Long chatRoomId, Long userId, Long roleId, String title) {
        this.chatRoomId = chatRoomId;
        this.userId = userId;
        this.roleId = roleId;
        this.title = title;
        this.isActive = true;
        this.joinOrder = 0;
        this.deleted = 0;
    }
}