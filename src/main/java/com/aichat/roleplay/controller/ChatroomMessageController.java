package com.aichat.roleplay.controller;

import com.aichat.roleplay.common.ApiResponse;
import com.aichat.roleplay.common.PagedResponse;
import com.aichat.roleplay.context.UserContext;
import com.aichat.roleplay.model.ChatroomMessage;
import com.aichat.roleplay.model.User;
import com.aichat.roleplay.service.IChatroomMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天室消息控制器
 * 提供聊天室消息相关的REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/chatrooms")
public class ChatroomMessageController {

    @Autowired
    private IChatroomMessageService chatroomMessageService;

    /**
     * 分页获取聊天室消息列表
     */
    @GetMapping("/{chatRoomId}/messages")
    public ApiResponse<PagedResponse<ChatroomMessage>> getChatroomMessages(
            @PathVariable Long chatRoomId,
            @RequestParam(required = false) Long lastMessageId,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.info("获取聊天室消息，聊天室ID: {}, lastMessageId: {}, pageSize: {}", chatRoomId, lastMessageId, pageSize);

        try {
            User user = getCurrentUser();
            log.debug("当前用户: {}", user.getUsername());

            // 获取分页消息列表
            List<ChatroomMessage> messages;
            if (lastMessageId != null) {
                messages = chatroomMessageService.getChatroomMessagesPage(chatRoomId, lastMessageId, pageSize);
            } else {
                // 如果没有指定lastMessageId，获取最新的消息
                messages = chatroomMessageService.getChatroomMessagesPage(chatRoomId, Long.MAX_VALUE, pageSize);
            }

            log.info("获取聊天室消息成功，消息数量: {}, 用户: {}, 聊天室ID: {}", 
                    messages.size(), user.getId(), chatRoomId);

            // 标记消息为已读（仅当前用户）
            chatroomMessageService.markAllMessagesAsRead(chatRoomId, user.getId());

            // 构造分页响应
            Long nextCursor = messages.isEmpty() ? null : messages.get(messages.size() - 1).getId();
            boolean hasMore = messages.size() == pageSize;

            PagedResponse<ChatroomMessage> response = new PagedResponse<>(messages, nextCursor, hasMore);
            return ApiResponse.success("获取聊天室消息成功", response);

        } catch (Exception e) {
            log.error("获取聊天室消息失败", e);
            return ApiResponse.error("获取聊天室消息失败: " + e.getMessage());
        }
    }

    /**
     * 获取聊天室所有消息（不分页）
     */
    @GetMapping("/{chatRoomId}/messages/all")
    public ApiResponse<List<ChatroomMessage>> getAllChatroomMessages(@PathVariable Long chatRoomId) {
        log.info("获取聊天室所有消息，聊天室ID: {}", chatRoomId);

        try {
            User user = getCurrentUser();
            List<ChatroomMessage> messages = chatroomMessageService.getChatroomMessages(chatRoomId);
            
            log.info("获取聊天室所有消息成功，消息数量: {}, 用户: {}, 聊天室ID: {}", 
                    messages.size(), user.getId(), chatRoomId);

            return ApiResponse.success("获取聊天室消息成功", messages);

        } catch (Exception e) {
            log.error("获取聊天室所有消息失败", e);
            return ApiResponse.error("获取聊天室消息失败: " + e.getMessage());
        }
    }

    /**
     * 获取聊天室未读消息
     */
    @GetMapping("/{chatRoomId}/messages/unread")
    public ApiResponse<List<ChatroomMessage>> getUnreadMessages(@PathVariable Long chatRoomId) {
        log.info("获取聊天室未读消息，聊天室ID: {}", chatRoomId);

        try {
            User user = getCurrentUser();
            List<ChatroomMessage> unreadMessages = chatroomMessageService.getUnreadMessages(chatRoomId, user.getId());
            
            log.info("获取聊天室未读消息成功，未读消息数量: {}, 用户: {}, 聊天室ID: {}", 
                    unreadMessages.size(), user.getId(), chatRoomId);

            return ApiResponse.success("获取未读消息成功", unreadMessages);

        } catch (Exception e) {
            log.error("获取聊天室未读消息失败", e);
            return ApiResponse.error("获取未读消息失败: " + e.getMessage());
        }
    }

    /**
     * 获取聊天室最新消息
     */
    @GetMapping("/{chatRoomId}/messages/latest")
    public ApiResponse<ChatroomMessage> getLatestMessage(@PathVariable Long chatRoomId) {
        log.info("获取聊天室最新消息，聊天室ID: {}", chatRoomId);

        try {
            ChatroomMessage latestMessage = chatroomMessageService.getLatestMessage(chatRoomId);
            
            if (latestMessage != null) {
                log.info("获取聊天室最新消息成功，消息ID: {}, 聊天室ID: {}", 
                        latestMessage.getId(), chatRoomId);
                return ApiResponse.success("获取最新消息成功", latestMessage);
            } else {
                log.info("聊天室暂无消息，聊天室ID: {}", chatRoomId);
                return ApiResponse.success("聊天室暂无消息", null);
            }

        } catch (Exception e) {
            log.error("获取聊天室最新消息失败", e);
            return ApiResponse.error("获取最新消息失败: " + e.getMessage());
        }
    }

    /**
     * 标记消息为已读
     */
    @PutMapping("/{chatRoomId}/messages/{messageId}/read")
    public ApiResponse<String> markMessageAsRead(
            @PathVariable Long chatRoomId,
            @PathVariable Long messageId) {
        log.info("标记消息为已读，聊天室ID: {}, 消息ID: {}", chatRoomId, messageId);

        try {
            User user = getCurrentUser();
            chatroomMessageService.markMessageAsRead(messageId);
            
            log.info("标记消息为已读成功，消息ID: {}, 用户: {}", messageId, user.getId());
            return ApiResponse.success("消息已标记为已读");

        } catch (Exception e) {
            log.error("标记消息为已读失败", e);
            return ApiResponse.error("标记消息为已读失败: " + e.getMessage());
        }
    }

    /**
     * 标记聊天室所有消息为已读
     */
    @PutMapping("/{chatRoomId}/messages/read-all")
    public ApiResponse<String> markAllMessagesAsRead(@PathVariable Long chatRoomId) {
        log.info("标记聊天室所有消息为已读，聊天室ID: {}", chatRoomId);

        try {
            User user = getCurrentUser();
            chatroomMessageService.markAllMessagesAsRead(chatRoomId, user.getId());
            
            log.info("标记聊天室所有消息为已读成功，聊天室ID: {}, 用户: {}", chatRoomId, user.getId());
            return ApiResponse.success("所有消息已标记为已读");

        } catch (Exception e) {
            log.error("标记聊天室所有消息为已读失败", e);
            return ApiResponse.error("标记所有消息为已读失败: " + e.getMessage());
        }
    }

    /**
     * 统计聊天室消息数量
     */
    @GetMapping("/{chatRoomId}/messages/count")
    public ApiResponse<Long> countMessages(@PathVariable Long chatRoomId) {
        log.info("统计聊天室消息数量，聊天室ID: {}", chatRoomId);

        try {
            Long messageCount = chatroomMessageService.countMessages(chatRoomId);
            
            log.info("统计聊天室消息数量成功，消息数量: {}, 聊天室ID: {}", messageCount, chatRoomId);
            return ApiResponse.success("获取消息数量成功", messageCount);

        } catch (Exception e) {
            log.error("统计聊天室消息数量失败", e);
            return ApiResponse.error("获取消息数量失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前登录用户
     */
    private User getCurrentUser() {
        User user = UserContext.getCurrentUser();
        if (user == null) {
            throw new RuntimeException("用户未登录或登录已过期");
        }
        return user;
    }
}