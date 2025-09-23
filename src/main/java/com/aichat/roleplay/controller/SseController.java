package com.aichat.roleplay.controller;

import com.aichat.roleplay.service.SseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sse")
public class SseController {

    @Autowired
    private SseService sseService;

    /**
     * 建立SSE连接，订阅特定聊天的实时消息
     *
     * @param chatId         聊天ID
     * @param authentication 用户认证信息
     * @return SSE发射器
     */
    @GetMapping("/subscribe/{chatId}")
    public SseEmitter subscribeToChat(@PathVariable Long chatId, Authentication authentication) {
        // 获取当前登录用户的用户名
        String username = authentication.getName();

        // 创建SSE连接并返回发射器
        return sseService.createConnection(username, chatId);
    }
}