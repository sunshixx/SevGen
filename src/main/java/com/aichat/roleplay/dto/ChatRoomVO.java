package com.aichat.roleplay.dto;

import com.aichat.roleplay.model.Chat;
import com.aichat.roleplay.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天室视图对象
 * 用于返回聊天室的完整信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomVO {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<Role> roles;
    private List<Long> roleIds;
    private Long messageCount;
    private LocalDateTime lastMessageTime;

    // 从Chat对象创建ChatRoomVO (已废弃，因为Chat不再包含chatRoomId)
    @Deprecated
    public static ChatRoomVO fromChat(Chat chat, List<Role> roles) {
        ChatRoomVO vo = new ChatRoomVO();
        vo.setId(chat.getId()); // 使用chat的ID而不是chatRoomId
        vo.setName(chat.getTitle());
        vo.setDescription(null); // Chat表中没有description字段，可以后续扩展
        vo.setCreateTime(chat.getCreateTime());
        vo.setUpdateTime(chat.getUpdateTime());
        vo.setRoles(roles);
        return vo;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }

    public Long getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Long messageCount) {
        this.messageCount = messageCount;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
}