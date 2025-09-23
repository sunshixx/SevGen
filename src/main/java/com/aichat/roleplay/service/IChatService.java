package com.aichat.roleplay.service;

import com.aichat.roleplay.model.Chat;

import java.util.List;
import java.util.Optional;

/**
 * 聊天会话服务接口
 * 遵循SOLID原则中的接口隔离原则
 * 定义聊天会话相关的业务操作
 */
public interface IChatService {

    /**
     * 创建聊天会话
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 创建的聊天会话
     */
    Chat createChat(Long userId, Long roleId);

    /**
     * 获取用户的所有聊天会话
     *
     * @param userId 用户ID
     * @return 聊天会话列表
     */
    List<Chat> getUserChats(Long userId);

    /**
     * 获取用户的活跃聊天会话
     *
     * @param userId 用户ID
     * @return 活跃聊天会话列表
     */
    List<Chat> getUserActiveChats(Long userId);

    /**
     * 根据ID获取聊天会话
     *
     * @param id 聊天会话ID
     * @return 聊天会话
     */
    Optional<Chat> getChatById(Long id);

    /**
     * 更新聊天会话
     *
     * @param chat 聊天会话
     * @return 更新后的聊天会话
     */
    Chat updateChat(Chat chat);

    /**
     * 停用聊天会话
     *
     * @param id 聊天会话ID
     */
    void deactivateChat(Long id);

    /**
     * 删除聊天会话
     *
     * @param id 聊天会话ID
     */
    void deleteChat(Long id);

    /**
     * 获取用户与特定角色的聊天会话
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 聊天会话列表
     */
    List<Chat> getUserChatsByRole(Long userId, Long roleId);
}