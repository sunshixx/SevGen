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

    List<Message> findByChatId(Long chatId);
}