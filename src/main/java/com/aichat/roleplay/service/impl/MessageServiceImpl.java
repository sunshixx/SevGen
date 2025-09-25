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
    private final SseService sseService;

    /**
     * 构造函数注入，遵循依赖倒置原则
     */
    @Autowired
    public MessageServiceImpl(MessageMapper messageMapper,
                              ChatMapper chatMapper,
                              IAiChatService aiChatService,
                              SseService sseService) {
        this.messageMapper = messageMapper;
        this.chatMapper = chatMapper;
        this.aiChatService = aiChatService;
        this.sseService = sseService;
    }

    @Override
    public Message sendUserMessage(Long chatId, String content) {
        log.info("发送用户消息，聊天ID: {}", chatId);

        // 验证聊天会话是否存在
        Chat chat = chatMapper.selectById(chatId);
        if (chat == null) {
            throw new RuntimeException("聊天会话不存在");
        }

        // 创建用户消息
        Message userMessage = Message.builder()
                .chatId(chatId)
                .senderType("user")
                .content(content)
                .isRead(false)
                .deleted(0)
                .build();

        // 保存用户消息
        int result = messageMapper.insert(userMessage);
        if (result > 0) {
            log.info("用户消息保存成功，消息ID: {}", userMessage.getId());
            return userMessage;
        } else {
            throw new RuntimeException("用户消息保存失败");
        }
    }

    @Override
    public Message generateAiResponse(Long chatId, String userMessage, Role role) {
        log.info("生成AI回复，聊天ID: {}, 角色: {}", chatId, role.getName());

        try {
            // 获取聊天历史
            List<Message> recentMessages = messageMapper.findByChatId(chatId);
            StringBuilder chatHistory = new StringBuilder();

            // 构建聊天历史（最近10条消息）
            int startIndex = Math.max(0, recentMessages.size() - 10);
            for (int i = startIndex; i < recentMessages.size() - 1; i++) { // 排除当前用户消息
                Message msg = recentMessages.get(i);
                chatHistory.append(msg.getSenderType()).append(": ").append(msg.getContent()).append("\n");
            }

            // 调用AI服务生成回复
            String aiResponse = aiChatService.generateCharacterResponse(
                    role.getName(),
                    role.getCharacterPrompt(),
                    userMessage
            );

            // 创建AI回复消息
            Message responseMessage = Message.builder()
                    .chatId(chatId)
                    .senderType("ai")
                    .content(aiResponse)
                    .isRead(false)
                    .deleted(0)
                    .build();

            // 保存AI回复消息
            int result = messageMapper.insert(responseMessage);
            if (result > 0) {
                log.info("AI回复保存成功，消息ID: {}", responseMessage.getId());

                // 通过SSE发送实时消息
                Chat chat = chatMapper.selectById(chatId);
                if (chat != null && chat.getUserId() != null) {
                    //sseService.sendMessage(chat.getUserId().toString(), chatId, responseMessage);
                }

                return responseMessage;
            } else {
                throw new RuntimeException("AI回复保存失败");
            }

        } catch (Exception e) {
            log.error("生成AI回复失败", e);
            throw new RuntimeException("生成AI回复失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getChatMessages(Long chatId) {
        log.debug("获取聊天消息，聊天ID: {}", chatId);
        return messageMapper.findByChatId(chatId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getUnreadMessages(Long chatId) {
        log.debug("获取未读消息，聊天ID: {}", chatId);
        return messageMapper.findUnreadByChatId(chatId);
    }

    @Override
    public void markMessagesAsRead(Long chatId) {
        log.info("标记消息为已读，聊天ID: {}", chatId);
        messageMapper.markAllAsReadByChatId(chatId);
    }

    @Override
    public void markMessageAsRead(Long messageId) {
        log.info("标记单条消息为已读，消息ID: {}", messageId);
        messageMapper.markAsRead(messageId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countChatMessages(Long chatId) {
        log.debug("统计聊天消息数量，聊天ID: {}", chatId);
        return messageMapper.countByChatId(chatId);
    }

    @Override
    @Transactional(readOnly = true)
    public Message getLatestMessage(Long chatId) {
        log.debug("获取最新消息，聊天ID: {}", chatId);
        return messageMapper.findLatestByChatId(chatId);
    }
}