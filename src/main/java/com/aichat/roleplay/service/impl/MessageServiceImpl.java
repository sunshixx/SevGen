package com.aichat.roleplay.service.impl;

import com.aichat.roleplay.mapper.ChatMapper;
import com.aichat.roleplay.mapper.MessageMapper;
import com.aichat.roleplay.model.Chat;
import com.aichat.roleplay.model.Message;
import com.aichat.roleplay.model.Role;
import com.aichat.roleplay.service.IAiChatService;
import com.aichat.roleplay.service.IMessageService;
import com.aichat.roleplay.service.SseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 消息服务实现类
 * 遵循SOLID原则中的单一职责原则和依赖倒置原则
 * 专门处理消息相关的业务逻辑
 */
@Service
@Transactional
public class MessageServiceImpl implements IMessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);

    private final MessageMapper messageMapper;
    private final ChatMapper chatMapper;
    private final IAiChatService aiChatService;


    /**
     * 构造函数注入，遵循依赖倒置原则
     */
    @Autowired
    public MessageServiceImpl(MessageMapper messageMapper,
                              ChatMapper chatMapper,
                              IAiChatService aiChatService
                             ) {
        this.messageMapper = messageMapper;
        this.chatMapper = chatMapper;
        this.aiChatService = aiChatService;

    }


    @Override
    public Message saveVoiceMessage(Long chatId, Long roleId, String senderType, String audioUrl, 
                                  String transcribedText, Integer audioDuration) {
        return saveVoiceMessage(chatId, roleId, senderType, transcribedText, audioUrl, transcribedText, audioDuration);
    }

    @Override
    public Message saveVoiceMessage(Long chatId, Long roleId, String senderType, String content,
                                  String audioUrl, String transcribedText, Integer audioDuration) {
        log.info("保存语音消息，聊天ID: {}, 发送者: {}, 音频时长: {}秒", chatId, senderType, audioDuration);

        // 验证聊天会话是否存在
        Chat chat = chatMapper.selectById(chatId);
        if (chat == null) {
            throw new RuntimeException("聊天会话不存在");
        }

        // 创建语音消息
        Message voiceMessage = Message.builder()
                .chatId(chatId)
                .roleId(roleId)
                .senderType(senderType)
                .content(content)
                .messageType("voice")
                .audioUrl(audioUrl)
                .transcribedText(transcribedText)
                .audioDuration(audioDuration)
                .isRead(false)
                .deleted(0)
                .build();

        // 保存语音消息
        int result = messageMapper.insert(voiceMessage);
        if (result > 0) {
            log.info("语音消息保存成功，消息ID: {}", voiceMessage.getId());
            return voiceMessage;
        } else {
            throw new RuntimeException("语音消息保存失败");
        }
    }

    @Override
    public List<Message> findByChatId(Long chatId) {
        // 返回所有历史消息，用于构建完整的聊天上下文
        return messageMapper.findByChatId(chatId);
    }
}