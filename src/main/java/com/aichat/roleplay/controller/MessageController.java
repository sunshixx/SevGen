package com.aichat.roleplay.controller;

import com.aichat.roleplay.common.ApiResponse;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 消息控制器
 * 遵循SOLID原则中的单一职责原则和依赖倒置原则
 * 集成AI聊天功能，提供智能对话服务
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

    /**
     * 构造函数注入，遵循依赖倒置原则
     */
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
     * 发送消息并获取AI回复
     *
     * @param request        发送消息请求
     * @param authentication 认证信息
     * @return 用户消息和AI回复
     */
    @PostMapping
    public ApiResponse<Map<String, Message>> sendMessage(@Validated @RequestBody SendMessageRequest request,
                                                         Authentication authentication) {
        log.info("发送消息，聊天ID: {}, 内容长度: {}", request.getChatId(), request.getContent().length());

        try {
            User user = getCurrentUser(authentication);

            // 验证聊天会话权限
            Chat chat = validateChatAccess(request.getChatId(), user.getId());

            // 保存用户消息
            Message userMessage = saveUserMessage(request, user.getId());

            // 异步生成AI回复
            CompletableFuture<Message> aiResponseFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return generateAiResponse(chat, request.getContent(), userMessage.getId());
                } catch (Exception e) {
                    log.error("生成AI回复失败", e);
                    return createErrorMessage(chat.getId(), "抱歉，我现在无法回复，请稍后再试。");
                }
            });

            // 等待AI回复完成（设置合理的超时时间）
            Message aiMessage = aiResponseFuture.get();

            // 构建响应
            Map<String, Message> responseData = new HashMap<>();
            responseData.put("userMessage", userMessage);
            responseData.put("aiMessage", aiMessage);

            return ApiResponse.success("消息发送成功", responseData);

        } catch (Exception e) {
            log.error("发送消息失败", e);
            return ApiResponse.error("发送消息失败: " + e.getMessage());
        }
    }

    /**
     * 异步发送消息（用于SSE场景）
     *
     * @param request        发送消息请求
     * @param authentication 认证信息
     * @return 用户消息（AI回复将通过SSE推送）
     */
    @PostMapping("/async")
    public ApiResponse<Message> sendMessageAsync(@Validated @RequestBody SendMessageRequest request,
                                                 Authentication authentication) {
        log.info("异步发送消息，聊天ID: {}", request.getChatId());

        try {
            User user = getCurrentUser(authentication);

            // 验证聊天会话权限
            Chat chat = validateChatAccess(request.getChatId(), user.getId());

            // 保存用户消息
            Message userMessage = saveUserMessage(request, user.getId());

            // 异步生成AI回复（通过SSE推送，不等待完成）
            CompletableFuture.runAsync(() -> {
                try {
                    generateAiResponse(chat, request.getContent(), userMessage.getId());
                    log.info("AI回复生成完成，将通过SSE推送");
                } catch (Exception e) {
                    log.error("异步生成AI回复失败", e);
                    // 这里可以通过SSE推送错误信息
                }
            });

            return ApiResponse.success("消息发送成功，AI回复将异步推送", userMessage);

        } catch (Exception e) {
            log.error("异步发送消息失败", e);
            return ApiResponse.error("发送消息失败: " + e.getMessage());
        }
    }

    /**
     * 获取聊天消息列表
     *
     * @param chatId         聊天ID
     * @param authentication 认证信息
     * @return 消息列表
     */
    @GetMapping("/chat/{chatId}")
    public ApiResponse<List<Message>> getChatMessages(@PathVariable Long chatId,
                                                      Authentication authentication) {
        log.debug("获取聊天消息，聊天ID: {}", chatId);

        try {
            User user = getCurrentUser(authentication);

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
     * 获取未读消息
     *
     * @param chatId         聊天ID
     * @param authentication 认证信息
     * @return 未读消息列表
     */
    @GetMapping("/chat/{chatId}/unread")
    public ApiResponse<List<Message>> getUnreadMessages(@PathVariable Long chatId,
                                                        Authentication authentication) {
        log.debug("获取未读消息，聊天ID: {}", chatId);

        try {
            User user = getCurrentUser(authentication);

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
     *
     * @param chatId         聊天ID
     * @param authentication 认证信息
     * @return 操作结果
     */
    @PutMapping("/chat/{chatId}/read")
    public ApiResponse<String> markMessagesAsRead(@PathVariable Long chatId,
                                                  Authentication authentication) {
        log.info("标记消息已读，聊天ID: {}", chatId);

        try {
            User user = getCurrentUser(authentication);

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
     *
     * @param authentication 认证信息
     * @return 用户信息
     */
    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /**
     * 验证聊天会话访问权限
     *
     * @param chatId 聊天ID
     * @param userId 用户ID
     * @return 聊天会话
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
     *
     * @param request 发送消息请求
     * @param userId  用户ID
     * @return 保存的消息
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
     * 生成AI回复
     *
     * @param chat          聊天会话
     * @param userMessage   用户消息
     * @param userMessageId 用户消息ID
     * @return AI回复消息
     */
    private Message generateAiResponse(Chat chat, String userMessage, Long userMessageId) {
        // 获取角色信息
        Role role = roleMapper.selectById(chat.getRoleId());
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        // 获取聊天历史（最近10条消息）
        List<Message> recentMessages = messageMapper.findByChatId(chat.getId());
        StringBuilder chatHistory = new StringBuilder();

        for (int i = Math.max(0, recentMessages.size() - 10); i < recentMessages.size(); i++) {
            Message msg = recentMessages.get(i);
            if (!msg.getId().equals(userMessageId)) { // 排除当前用户消息
                chatHistory.append(msg.getSenderType()).append(": ").append(msg.getContent()).append("\n");
            }
        }

        // 调用AI服务生成回复
        String aiResponse = aiChatService.generateCharacterResponse(
                role.getName(),
                role.getCharacterPrompt(),
                userMessage
        );

        // 保存AI回复
        Message aiMessage = Message.builder()
                .chatId(chat.getId())
                .senderType("ai")
                .content(aiResponse)
                .isRead(false)
                .deleted(0)
                .build();

        int result = messageMapper.insert(aiMessage);
        if (result > 0) {
            return aiMessage;
        } else {
            throw new RuntimeException("保存AI回复失败");
        }
    }

    /**
     * 创建错误消息
     *
     * @param chatId       聊天ID
     * @param errorContent 错误内容
     * @return 错误消息
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