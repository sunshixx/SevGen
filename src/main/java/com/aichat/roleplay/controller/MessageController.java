package com.aichat.roleplay.controller;

import com.aichat.roleplay.common.ApiResponse;
import com.aichat.roleplay.common.PagedResponse;
import com.aichat.roleplay.context.UserContext;
import com.aichat.roleplay.dto.SendMessageRequest;
import com.aichat.roleplay.mapper.ChatMapper;
import com.aichat.roleplay.mapper.MessageMapper;
import com.aichat.roleplay.mapper.RoleMapper;
import com.aichat.roleplay.model.Chat;
import com.aichat.roleplay.model.Message;
import com.aichat.roleplay.model.Role;
import com.aichat.roleplay.model.User;
import com.aichat.roleplay.service.IAiChatService;
import com.aichat.roleplay.service.IUserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 消息控制器
 * 提供聊天消息管理和AI对话功能
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);

    private final MessageMapper messageMapper;
    private final ChatMapper chatMapper;
    private final RoleMapper roleMapper;
    private final IUserService userService;
    private final IAiChatService aiChatService;

    @Autowired
    public MessageController(MessageMapper messageMapper,
                             ChatMapper chatMapper,
                             RoleMapper roleMapper,
                             IUserService userService,
                             IAiChatService aiChatService) {
        this.messageMapper = messageMapper;
        this.chatMapper = chatMapper;
        this.roleMapper = roleMapper;
        this.userService = userService;
        this.aiChatService = aiChatService;
    }

    /**
     * 获取聊天消息列表
     */
    @GetMapping("/chat/{chatId}")
    public ApiResponse<List<Message>> getChatMessages(@PathVariable Long chatId) {
        log.debug("获取聊天消息，聊天ID: {}", chatId);

        try {
            User user = getCurrentUser();

            // 验证聊天会话权限
            validateChatAccess(chatId, user.getId());

            // 获取消息列表
            List<Message> messages = messageMapper.findByChatId(chatId);

            // 标记消息为已读
            messageMapper.markAllAsReadByChatId(chatId);

            return ApiResponse.success("获取聊天消息成功", messages);

        } catch (Exception e) {
            log.error("获取聊天消息失败", e);
            return ApiResponse.error("获取聊天消息失败: " + e.getMessage());
        }
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
     * 获取未读消息
     */
    @GetMapping("/chat/{chatId}/unread")
    public ApiResponse<List<Message>> getUnreadMessages(@PathVariable Long chatId) {
        log.debug("获取未读消息，聊天ID: {}", chatId);

        try {
            User user = getCurrentUser();

            // 验证聊天会话权限
            validateChatAccess(chatId, user.getId());

            List<Message> messages = messageMapper.findUnreadByChatId(chatId);

            return ApiResponse.success("获取未读消息成功", messages);

        } catch (Exception e) {
            log.error("获取未读消息失败", e);
            return ApiResponse.error("获取未读消息失败: " + e.getMessage());
        }
    }

    /**
     * 标记消息为已读
     */
    @PutMapping("/chat/{chatId}/read")
    public ApiResponse<String> markMessagesAsRead(@PathVariable Long chatId) {
        log.info("标记消息已读，聊天ID: {}", chatId);

        try {
            User user = getCurrentUser();

            // 验证聊天会话权限
            validateChatAccess(chatId, user.getId());

            messageMapper.markAllAsReadByChatId(chatId);

            return ApiResponse.success("消息已标记为已读");

        } catch (Exception e) {
            log.error("标记消息已读失败", e);
            return ApiResponse.error("标记消息已读失败: " + e.getMessage());
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

    /**
     * 保存用户消息
     */
    private Message saveUserMessage(SendMessageRequest request, Long userId) {
        Message message = Message.builder()
                .chatId(request.getChatId())
                .senderType("user")
                .content(request.getContent())
                .isRead(false)
                .deleted(0)
                .build();

        int result = messageMapper.insert(message);
        if (result > 0) {
            return message;
        } else {
            throw new RuntimeException("保存用户消息失败");
        }
    }


    /**
     * 创建错误消息
     */
    private Message createErrorMessage(Long chatId, String errorContent) {
        Message errorMessage = Message.builder()
                .chatId(chatId)
                .senderType("ai")
                .content(errorContent)
                .isRead(false)
                .deleted(0)
                .build();

        messageMapper.insert(errorMessage);
        return errorMessage;
    }
}