package com.aichat.roleplay.controller;

import com.aichat.roleplay.common.ApiResponse;
import com.aichat.roleplay.context.UserContext;
import com.aichat.roleplay.dto.ChatVO;
import com.aichat.roleplay.dto.CreateChatRequest;
import com.aichat.roleplay.model.Chat;
import com.aichat.roleplay.model.User;
import com.aichat.roleplay.service.IChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 聊天会话控制器
 * 专门处理单agent聊天功能
 * 已废弃，聊天使用ssecontroller来实现，该控制器仅留作后续拓展同步调用使用！！！！！
 */
@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private IChatService chatService;

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
     * 分页获取用户的聊天会话列表
     */
    @GetMapping("/list")
    public ApiResponse<List<ChatVO>> getUserChats(
            @RequestParam(required = false) LocalDateTime lastUpdatedAt,
            @RequestParam(defaultValue = "20") int pageSize) {
        User user = getCurrentUser();
        log.debug("获取用户聊天会话列表，用户: {}, lastUpdatedAt: {}, pageSize: {}",
                user.getUsername(), lastUpdatedAt, pageSize);

        try {
            List<Chat> chats = chatService.getUserChats(user.getId(), lastUpdatedAt, pageSize);
            List<ChatVO> chatsVO = ChatVO.po2voList(chats);
            return ApiResponse.success("获取聊天会话列表成功", chatsVO);
        } catch (Exception e) {
            log.error("获取聊天会话列表失败", e);
            return ApiResponse.error("获取聊天会话列表失败: " + e.getMessage());
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
     * 获取当前用户
     */
    private User getCurrentUser() {
        User user = UserContext.getCurrentUser();
        if (user == null) {
            throw new RuntimeException("用户未登录");
        }
        return user;
    }
}