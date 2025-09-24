package com.aichat.roleplay.controller;

import com.aichat.roleplay.common.ApiResponse;
import com.aichat.roleplay.context.UserContext;
import com.aichat.roleplay.dto.CreateChatRequest;
import com.aichat.roleplay.model.Chat;
import com.aichat.roleplay.model.User;
import com.aichat.roleplay.service.IChatService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天会话控制器
 * 提供聊天会话的创建、管理功能
 */
@RestController
@RequestMapping("/api/chats")
@AllArgsConstructor
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final IChatService chatService;

//    @Autowired
//    public ChatController(IChatService chatService) {
//        this.chatService = chatService;
//    }

    /**
     * 创建新的聊天会话
     */
    @PostMapping
    public ApiResponse<Chat> createChat(@Validated @RequestBody CreateChatRequest request) {
        User user = getCurrentUser();
        log.info("创建聊天会话，角色ID: {}, 用户: {}", request.getRoleId(), user.getUsername());

        try {
            // 创建聊天会话
            Chat chat = chatService.createChat(user.getId(), request.getRoleId());

            // 如果有自定义标题，更新聊天标题
            if (request.getTitle() != null && !request.getTitle().isEmpty()) {
                chat.setTitle(request.getTitle());
                chat = chatService.updateChat(chat);
            }

            return ApiResponse.success("聊天会话创建成功", chat);

        } catch (Exception e) {
            log.error("创建聊天会话失败", e);
            return ApiResponse.error("创建聊天会话失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的所有聊天会话
     */
    @GetMapping
    public ApiResponse<List<Chat>> getUserChats() {
        User user = getCurrentUser();
        log.debug("获取用户聊天会话列表，用户: {}", user.getUsername());

        try {
            List<Chat> chats = chatService.getUserChats(user.getId());
            return ApiResponse.success("获取聊天会话列表成功", chats);

        } catch (Exception e) {
            log.error("获取聊天会话列表失败", e);
            return ApiResponse.error("获取聊天会话列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的活跃聊天会话
     */
    @GetMapping("/active")
    public ApiResponse<List<Chat>> getUserActiveChats() {
        User user = getCurrentUser();
        log.debug("获取用户活跃聊天会话，用户: {}", user.getUsername());

        try {
            List<Chat> chats = chatService.getUserActiveChats(user.getId());
            return ApiResponse.success("获取活跃聊天会话成功", chats);

        } catch (Exception e) {
            log.error("获取活跃聊天会话失败", e);
            return ApiResponse.error("获取活跃聊天会话失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取聊天会话详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Chat> getChatById(@PathVariable Long id) {
        log.debug("获取聊天会话详情，会话ID: {}", id);

        try {
            User user = getCurrentUser();
            Chat chat = chatService.getChatById(id)
                    .orElseThrow(() -> new RuntimeException("聊天会话不存在"));

            // 验证聊天会话所有权
            if (!chat.getUserId().equals(user.getId())) {
                return ApiResponse.error(403, "无权访问该聊天会话");
            }

            return ApiResponse.success("获取聊天会话详情成功", chat);

        } catch (Exception e) {
            log.error("获取聊天会话详情失败", e);
            return ApiResponse.error("获取聊天会话详情失败: " + e.getMessage());
        }
    }

    /**
     * 更新聊天会话
     */
    @PutMapping("/{id}")
    public ApiResponse<Chat> updateChat(@PathVariable Long id,
                                        @RequestBody Chat chat) {
        log.info("更新聊天会话，会话ID: {}", id);

        try {
            User user = getCurrentUser();

            // 验证聊天会话存在性和所有权
            Chat existingChat = chatService.getChatById(id)
                    .orElseThrow(() -> new RuntimeException("聊天会话不存在"));

            if (!existingChat.getUserId().equals(user.getId())) {
                return ApiResponse.error(403, "无权更新该聊天会话");
            }

            chat.setId(id);
            chat.setUserId(user.getId()); // 确保不能修改所有者
            Chat updatedChat = chatService.updateChat(chat);

            return ApiResponse.success("聊天会话更新成功", updatedChat);

        } catch (Exception e) {
            log.error("更新聊天会话失败", e);
            return ApiResponse.error("更新聊天会话失败: " + e.getMessage());
        }
    }

    /**
     * 停用聊天会话
     */
    @PutMapping("/{id}/deactivate")
    public ApiResponse<String> deactivateChat(@PathVariable Long id) {
        log.info("停用聊天会话，会话ID: {}", id);

        try {
            User user = getCurrentUser();
            Chat chat = chatService.getChatById(id)
                    .orElseThrow(() -> new RuntimeException("聊天会话不存在"));

            if (!chat.getUserId().equals(user.getId())) {
                return ApiResponse.error(403, "无权停用该聊天会话");
            }

            chatService.deactivateChat(id);
            return ApiResponse.success("聊天会话停用成功");

        } catch (Exception e) {
            log.error("停用聊天会话失败", e);
            return ApiResponse.error("停用聊天会话失败: " + e.getMessage());
        }
    }

    /**
     * 删除聊天会话
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteChat(@PathVariable Long id) {
        log.info("删除聊天会话，会话ID: {}", id);

        try {
            User user = getCurrentUser();
            Chat chat = chatService.getChatById(id)
                    .orElseThrow(() -> new RuntimeException("聊天会话不存在"));

            if (!chat.getUserId().equals(user.getId())) {
                return ApiResponse.error(403, "无权删除该聊天会话");
            }

            chatService.deleteChat(id);
            return ApiResponse.success("聊天会话删除成功");

        } catch (Exception e) {
            log.error("删除聊天会话失败", e);
            return ApiResponse.error("删除聊天会话失败: " + e.getMessage());
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