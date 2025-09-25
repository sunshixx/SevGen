package com.aichat.roleplay.service;

import com.aichat.roleplay.model.Message;
import com.aichat.roleplay.model.Role;

import java.util.List;

/**
 * 消息服务接口
 * 遵循SOLID原则中的接口隔离原则
 * 定义消息相关的业务操作
 */
public interface IMessageService {

    /**
     * 发送用户消息
     *
     * @param chatId  聊天ID
     * @param content 消息内容
     * @return 保存的消息
     */
    Message sendUserMessage(Long chatId, String content);

    /**
     * 生成AI回复
     *
     * @param chatId      聊天ID
     * @param userMessage 用户消息
     * @param role        角色信息
     * @return AI回复消息
     */
    Message generateAiResponse(Long chatId, String userMessage, Role role);

    /**
     * 获取聊天消息列表
     *
     * @param chatId 聊天ID
     * @return 消息列表
     */
    List<Message> getChatMessages(Long chatId);

    /**
     * 获取未读消息
     *
     * @param chatId 聊天ID
     * @return 未读消息列表
     */
    List<Message> getUnreadMessages(Long chatId);

    /**
     * 标记聊天的所有消息为已读
     *
     * @param chatId 聊天ID
     */
    void markMessagesAsRead(Long chatId);

    /**
     * 标记单条消息为已读
     *
     * @param messageId 消息ID
     */
    void markMessageAsRead(Long messageId);

    /**
     * 统计聊天消息数量
     *
     * @param chatId 聊天ID
     * @return 消息数量
     */
    Long countChatMessages(Long chatId);

    /**
     * 获取最新消息
     *
     * @param chatId 聊天ID
     * @return 最新消息
     */
    Message getLatestMessage(Long chatId);

    /**
     * 保存语音消息
     *
     * @param chatId         聊天ID
     * @param roleId         角色ID
     * @param senderType     发送者类型
     * @param audioUrl       音频URL
     * @param transcribedText 转文字内容
     * @param audioDuration  音频时长
     * @return 保存的消息
     */
    Message saveVoiceMessage(Long chatId, Long roleId, String senderType, String audioUrl, 
                           String transcribedText, Integer audioDuration);

    /**
     * 保存语音消息（重载方法，支持内容和转文字分开）
     *
     * @param chatId         聊天ID  
     * @param roleId         角色ID
     * @param senderType     发送者类型
     * @param content        消息内容（AI回复文本）
     * @param audioUrl       音频URL
     * @param transcribedText 转文字内容（用户语音转文字）
     * @param audioDuration  音频时长
     * @return 保存的消息
     */
    Message saveVoiceMessage(Long chatId, Long roleId, String senderType, String content,
                           String audioUrl, String transcribedText, Integer audioDuration);
}