package com.aichat.roleplay.controller;

import com.aichat.roleplay.common.ApiResponse;
import com.aichat.roleplay.common.PagedResponse;
import com.aichat.roleplay.context.UserContext;
import com.aichat.roleplay.mapper.ChatMapper;
import com.aichat.roleplay.mapper.MessageMapper;
import com.aichat.roleplay.model.Chat;
import com.aichat.roleplay.model.Message;
import com.aichat.roleplay.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息控制器
 * 提供聊天消息管理功能
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);

    private final MessageMapper messageMapper;
    private final ChatMapper chatMapper;

    @Autowired
    public MessageController(MessageMapper messageMapper,
                             ChatMapper chatMapper) {
        this.messageMapper = messageMapper;
        this.chatMapper = chatMapper;
    }

    /**
     * 分页获取聊天消息列表
     */
    @GetMapping("/chat/{chatId}/messages")
    public ApiResponse<PagedResponse<Message>> getChatMessages(
            @PathVariable Long chatId,
            @RequestParam(required = false) Long lastMessageId,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.debug("获取聊天消息，聊天ID: {}, lastMessageId: {}, pageSize: {}", chatId, lastMessageId, pageSize);

        try {
            User user = getCurrentUser();

            // 验证聊天会话权限
            validateChatAccess(chatId, user.getId());

            // 获取分页消息列表
            List<Message> messages = messageMapper.findByChatIdPage(chatId, lastMessageId, pageSize);
            log.info("获取分页消息列表成功，: {},用户：{},chatId:{}", messages,user.getId(),chatId);
            // 标记消息为已读（仅当前用户）
            messageMapper.markAllAsReadByChatId(chatId);

            // 构造分页响应
            Long nextCursor = messages.isEmpty() ? null : messages.get(messages.size() - 1).getId();
            boolean hasMore = messages.size() == pageSize;

            PagedResponse<Message> response = new PagedResponse<>(messages, nextCursor, hasMore);
            return ApiResponse.success("获取聊天消息成功", response);

        } catch (Exception e) {
            log.error("获取聊天消息失败", e);
            return ApiResponse.error("获取聊天消息失败: " + e.getMessage());
        }
    }

    /**
     * 分页获取聊天室消息列表
     */
    @GetMapping("/chatroom/{chatRoomId}/messages")
    public ApiResponse<PagedResponse<Message>> getChatRoomMessages(
            @PathVariable Long chatRoomId,
            @RequestParam(required = false) Long lastMessageId,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.debug("获取聊天室消息，聊天室ID: {}, lastMessageId: {}, pageSize: {}", chatRoomId, lastMessageId, pageSize);

        try {
            User user = getCurrentUser();
            
            // 验证用户是否有权限访问该聊天室
            List<Chat> chatRoomChats = chatMapper.findByChatRoomId(chatRoomId);
            boolean hasAccess = chatRoomChats.stream()
                    .anyMatch(chat -> chat.getUserId().equals(user.getId()));
            
            if (!hasAccess) {
                return ApiResponse.error("无权限访问该聊天室");
            }

            // 获取消息列表 - 使用现有的findByChatIdPage方法，因为chatId在概念上就是chatRoomId
            List<Message> messages = messageMapper.findByChatIdPage(chatRoomId, lastMessageId, pageSize);
            
            // 标记消息为已读
            messageMapper.markAllAsReadByChatId(chatRoomId);
            
            // 构造分页响应
            Long nextCursor = messages.isEmpty() ? null : messages.get(messages.size() - 1).getId();
            boolean hasMore = messages.size() == pageSize;

            PagedResponse<Message> response = new PagedResponse<>(messages, nextCursor, hasMore);
            return ApiResponse.success("获取聊天室消息成功", response);
        } catch (Exception e) {
            log.error("获取聊天室消息失败", e);
            return ApiResponse.error("获取聊天室消息失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户
     */
    private User getCurrentUser() {
        User user = UserContext.getCurrentUser();
        if (user == null) {
            throw new RuntimeException("用户未登录");
        }
        return user;
    }

    /**
     * 验证聊天会话访问权限
     */
    private Chat validateChatAccess(Long chatId, Long userId) {
        Chat chat = chatMapper.selectById(chatId);
        if (chat == null) {
            throw new RuntimeException("聊天会话不存在");
        }

        if (!chat.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问该聊天会话");
        }

        return chat;
    }
}