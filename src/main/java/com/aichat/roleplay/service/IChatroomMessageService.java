package com.aichat.roleplay.service;

import com.aichat.roleplay.model.ChatroomMessage;

import java.util.List;

/**
 * 聊天室消息服务接口
 * 定义聊天室消息相关的业务操作
 */
public interface IChatroomMessageService {

    /**
     * 保存用户消息
     *
     * @param chatRoomId 聊天室ID
     * @param userId     用户ID
     * @param content    消息内容
     * @return 保存的消息
     */
    ChatroomMessage saveUserMessage(Long chatRoomId, Long userId, String content);

    /**
     * 保存AI消息
     *
     * @param chatRoomId 聊天室ID
     * @param roleId     角色ID
     * @param content    消息内容
     * @return 保存的消息
     */
    ChatroomMessage saveAiMessage(Long chatRoomId, Long roleId, String content);

    /**
     * 保存语音消息
     *
     * @param chatRoomId      聊天室ID
     * @param userId          用户ID（用户消息时使用）
     * @param roleId          角色ID（AI消息时使用）
     * @param senderType      发送者类型
     * @param content         消息内容
     * @param audioUrl        音频URL
     * @param transcribedText 转文字内容
     * @param audioDuration   音频时长
     * @return 保存的消息
     */
    ChatroomMessage saveVoiceMessage(Long chatRoomId, Long userId, Long roleId, String senderType,
                                     String content, String audioUrl, String transcribedText, Integer audioDuration);

    /**
     * 获取聊天室消息列表
     *
     * @param chatRoomId 聊天室ID
     * @return 消息列表
     */
    List<ChatroomMessage> getChatroomMessages(Long chatRoomId);

    /**
     * 分页获取聊天室消息
     *
     * @param chatRoomId    聊天室ID
     * @param lastMessageId 最后一条消息ID（游标分页）
     * @param pageSize      每页数量
     * @return 消息列表
     */
    List<ChatroomMessage> getChatroomMessagesPage(Long chatRoomId, Long lastMessageId, int pageSize);

    /**
     * 获取聊天室未读消息
     *
     * @param chatRoomId 聊天室ID
     * @param userId     用户ID
     * @return 未读消息列表
     */
    List<ChatroomMessage> getUnreadMessages(Long chatRoomId, Long userId);

    /**
     * 标记消息为已读
     *
     * @param messageId 消息ID
     */
    void markMessageAsRead(Long messageId);

    /**
     * 标记聊天室所有消息为已读
     *
     * @param chatRoomId 聊天室ID
     * @param userId     用户ID
     */
    void markAllMessagesAsRead(Long chatRoomId, Long userId);

    /**
     * 获取聊天室最新消息
     *
     * @param chatRoomId 聊天室ID
     * @return 最新消息
     */
    ChatroomMessage getLatestMessage(Long chatRoomId);

    /**
     * 统计聊天室消息数量
     *
     * @param chatRoomId 聊天室ID
     * @return 消息数量
     */
    Long countMessages(Long chatRoomId);

    /**
     * 获取聊天室最近N条消息（用于构建聊天历史）
     *
     * @param chatRoomId 聊天室ID
     * @param limit      消息数量限制
     * @return 消息列表（按时间倒序）
     */
    List<ChatroomMessage> getRecentMessages(Long chatRoomId, int limit);

    /**
     * 构建聊天历史字符串（用于AI上下文）
     *
     * @param chatRoomId 聊天室ID
     * @param limit      消息数量限制
     * @return 聊天历史字符串
     */
    String buildChatHistory(Long chatRoomId, int limit);

    /**
     * 构建特定角色的聊天历史字符串（用于AI上下文）
     * 包含该角色之前的对话和其他角色的对话
     *
     * @param chatRoomId 聊天室ID
     * @param roleId     角色ID
     * @param limit      消息数量限制
     * @return 聊天历史字符串
     */
    String buildRoleChatHistory(Long chatRoomId, Long roleId, int limit);
}