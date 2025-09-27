package com.aichat.roleplay.service;

import com.aichat.roleplay.model.Chat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 聊天会话服务接口
 * 专门处理单agent聊天功能
 */
public interface IChatService {

    /**
     * 创建聊天会话（单角色）
     */
    Chat createChat(Long userId, Long roleId);

    /**
     * 获取用户的所有聊天会话
     */
    List<Chat> getUserChats(Long userId);

    /**
     * 获取用户最新的聊天会话
     */
    List<Chat> getUserChats(Long userId, LocalDateTime lastUpdatedAt, int pageSize);

    /**
     * 获取用户的活跃聊天会话
     *
     * @param userId 用户ID
     * @return 活跃聊天会话列表
     */
    List<Chat> getUserActiveChats(Long userId);

    /**
     * 根据ID获取聊天会话
     */
    Optional<Chat> getChatById(Long id);

    /**
     * 更新聊天会话
     */
    Chat updateChat(Chat chat);

    /**
     * 删除聊天会话
     */
    void deleteChat(Long id);

    /**
     * 获取用户与特定角色的聊天会话
     */
    List<Chat> getUserChatsByRole(Long userId, Long roleId);
}