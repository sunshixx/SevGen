package com.aichat.roleplay.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天室角色关联实体类
 * 管理聊天室和角色的多对多关系
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("chat_room_roles")
public class ChatRoomRole implements Serializable {

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
     * 角色ID
     */
    @TableField("role_id")
    private Long roleId;

    /**
     * 创建时间，自动填充
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 逻辑删除标志
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * 构造函数
     */
    public ChatRoomRole(Long chatRoomId, Long roleId) {
        this.chatRoomId = chatRoomId;
        this.roleId = roleId;
        this.deleted = 0;
    }
}