package com.aichat.roleplay.controller;

import com.aichat.roleplay.common.ApiResponse;
import com.aichat.roleplay.context.UserContext;
import com.aichat.roleplay.dto.ChatSessionVO;
import com.aichat.roleplay.model.User;
import com.aichat.roleplay.service.IChatService;
import com.aichat.roleplay.service.SseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sse")
public class SseController {

    @Autowired
    private SseService sseService;
    @Autowired
    private IChatService chatService;

    /**
     * 建立SSE连接，订阅特定聊天的实时消息
     *
     * @param chatId 聊天ID
     * @return SSE发射器
     */
    @GetMapping("/subscribe/{chatId}")
    public SseEmitter subscribeToChat(@PathVariable Long chatId) {
        // 获取当前登录用户
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }

        // 创建SSE连接并返回发射器
        return sseService.createConnection(currentUser.getUsername(), chatId);
    }
//    @GetMapping("/create")
//    public ApiResponse<ChatSessionVO > create(@PathVariable String  query) {
//        ChatSessionVO chatSessionVO = chatService.create(query);
//        return ApiResponse.success(chatSessionVO);
//    }
}