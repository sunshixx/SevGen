package com.aichat.roleplay.service.impl;

import com.aichat.roleplay.mapper.ChatroomMessageMapper;
import com.aichat.roleplay.model.ChatroomMessage;
import com.aichat.roleplay.service.IChatroomMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 聊天室消息服务实现类
 * 实现聊天室消息相关的业务逻辑
 */
@Slf4j
@Service
@Transactional
public class ChatroomMessageServiceImpl implements IChatroomMessageService {

    @Autowired
    private ChatroomMessageMapper chatroomMessageMapper;

    @Override
    public ChatroomMessage saveUserMessage(Long chatRoomId, Long userId, String content) {
        log.info("保存用户消息，聊天室ID: {}, 用户ID: {}", chatRoomId, userId);

        ChatroomMessage userMessage = ChatroomMessage.builder()
                .chatRoomId(chatRoomId)
                .userId(userId)
                .senderType("user")
                .content(content)
                .messageType("text")
                .isRead(false)
                .deleted(0)
                .build();

        int result = chatroomMessageMapper.insert(userMessage);
        if (result > 0) {
            log.info("用户消息保存成功，消息ID: {}", userMessage.getId());
            return userMessage;
        } else {
            log.error("用户消息保存失败");
            throw new RuntimeException("用户消息保存失败");
        }
    }

    @Override
    public ChatroomMessage saveAiMessage(Long chatRoomId, Long roleId, String content) {
        log.info("保存AI消息，聊天室ID: {}, 角色ID: {}", chatRoomId, roleId);

        ChatroomMessage aiMessage = ChatroomMessage.builder()
                .chatRoomId(chatRoomId)
                .roleId(roleId)
                .senderType("ai")
                .content(content)
                .messageType("text")
                .isRead(false)
                .deleted(0)
                .build();

        int result = chatroomMessageMapper.insert(aiMessage);
        if (result > 0) {
            log.info("AI消息保存成功，消息ID: {}", aiMessage.getId());
            return aiMessage;
        } else {
            log.error("AI消息保存失败");
            throw new RuntimeException("AI消息保存失败");
        }
    }

    @Override
    public ChatroomMessage saveVoiceMessage(Long chatRoomId, Long userId, Long roleId, String senderType,
                                            String content, String audioUrl, String transcribedText, Integer audioDuration) {
        log.info("保存语音消息，聊天室ID: {}, 发送者类型: {}, 音频时长: {}秒", chatRoomId, senderType, audioDuration);

        ChatroomMessage voiceMessage = ChatroomMessage.builder()
                .chatRoomId(chatRoomId)
                .userId(userId)
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

        int result = chatroomMessageMapper.insert(voiceMessage);
        if (result > 0) {
            log.info("语音消息保存成功，消息ID: {}", voiceMessage.getId());
            return voiceMessage;
        } else {
            log.error("语音消息保存失败");
            throw new RuntimeException("语音消息保存失败");
        }
    }

    @Override
    public List<ChatroomMessage> getChatroomMessages(Long chatRoomId) {
        log.info("获取聊天室消息，聊天室ID: {}", chatRoomId);
        return chatroomMessageMapper.findByChatRoomId(chatRoomId);
    }

    @Override
    public List<ChatroomMessage> getChatroomMessagesPage(Long chatRoomId, Long lastMessageId, int pageSize) {
        log.info("分页获取聊天室消息，聊天室ID: {}, 最后消息ID: {}, 页大小: {}", chatRoomId, lastMessageId, pageSize);
        return chatroomMessageMapper.findByChatRoomIdPage(chatRoomId, lastMessageId, pageSize);
    }

    @Override
    public List<ChatroomMessage> getUnreadMessages(Long chatRoomId, Long userId) {
        log.info("获取聊天室未读消息，聊天室ID: {}, 用户ID: {}", chatRoomId, userId);
        return chatroomMessageMapper.findUnreadByChatRoomId(chatRoomId, userId);
    }

    @Override
    public void markMessageAsRead(Long messageId) {
        log.info("标记消息为已读，消息ID: {}", messageId);
        chatroomMessageMapper.markAsRead(messageId);
    }

    @Override
    public void markAllMessagesAsRead(Long chatRoomId, Long userId) {
        log.info("标记聊天室所有消息为已读，聊天室ID: {}, 用户ID: {}", chatRoomId, userId);
        chatroomMessageMapper.markAllAsReadByChatRoomId(chatRoomId, userId);
    }

    @Override
    public ChatroomMessage getLatestMessage(Long chatRoomId) {
        log.info("获取聊天室最新消息，聊天室ID: {}", chatRoomId);
        return chatroomMessageMapper.findLatestByChatRoomId(chatRoomId);
    }

    @Override
    public Long countMessages(Long chatRoomId) {
        log.info("统计聊天室消息数量，聊天室ID: {}", chatRoomId);
        return chatroomMessageMapper.countByChatRoomId(chatRoomId);
    }

    @Override
    public List<ChatroomMessage> getRecentMessages(Long chatRoomId, int limit) {
        log.info("获取聊天室最近消息，聊天室ID: {}, 限制数量: {}", chatRoomId, limit);
        List<ChatroomMessage> messages = chatroomMessageMapper.findRecentByChatRoomId(chatRoomId, limit);
        // 将消息按时间正序排列（最早的在前面）
        Collections.reverse(messages);
        return messages;
    }

    @Override
    public String buildChatHistory(Long chatRoomId, int limit) {
        log.info("构建聊天历史，聊天室ID: {}, 限制数量: {}", chatRoomId, limit);
        
        List<ChatroomMessage> recentMessages = getRecentMessages(chatRoomId, limit);
        if (recentMessages == null || recentMessages.isEmpty()) {
            return "";
        }

        StringBuilder historyBuilder = new StringBuilder();
        for (ChatroomMessage message : recentMessages) {
            String sender;
            if ("user".equals(message.getSenderType())) {
                sender = "用户";
            } else {
                sender = "AI"; // 可以根据roleId获取角色名称，这里简化处理
            }
            historyBuilder.append(sender).append(": ").append(message.getContent()).append("\n");
        }

        String history = historyBuilder.toString().trim();
        log.debug("构建的聊天历史长度: {}", history.length());
        return history;
    }

    @Override
    public String buildRoleChatHistory(Long chatRoomId, Long roleId, int limit) {
        log.info("构建角色聊天历史，聊天室ID: {}, 角色ID: {}, 限制数量: {}", chatRoomId, roleId, limit);
        
        List<ChatroomMessage> recentMessages = getRecentMessages(chatRoomId, limit);
        if (recentMessages == null || recentMessages.isEmpty()) {
            return "";
        }

        StringBuilder historyBuilder = new StringBuilder();
        for (ChatroomMessage message : recentMessages) {
            String sender;
            if ("user".equals(message.getSenderType())) {
                sender = "用户";
            } else if (message.getRoleId() != null) {
                if (message.getRoleId().equals(roleId)) {
                    sender = "我"; // 当前角色的历史消息
                } else {
                    sender = "其他AI角色"; // 其他角色的消息，可以根据roleId获取具体角色名称
                }
            } else {
                sender = "AI";
            }
            historyBuilder.append(sender).append(": ").append(message.getContent()).append("\n");
        }

        String history = historyBuilder.toString().trim();
        log.debug("构建的角色聊天历史长度: {}", history.length());
        return history;
    }
}