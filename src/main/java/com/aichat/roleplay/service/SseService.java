package com.aichat.roleplay.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseService {

    // 存储每个用户的SSE连接
    private final Map<String, Map<Long, SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    // 创建SSE连接
    public SseEmitter createConnection(String username, Long chatId) {
        // 设置超时时间（30分钟）
        SseEmitter emitter = new SseEmitter(1800000L);

        // 清理资源的回调
        emitter.onCompletion(() -> removeConnection(username, chatId));
        emitter.onTimeout(() -> removeConnection(username, chatId));
        emitter.onError(e -> removeConnection(username, chatId));

        // 存储连接
        userEmitters.computeIfAbsent(username, k -> new ConcurrentHashMap<>())
                .put(chatId, emitter);

        return emitter;
    }

    // 发送消息到特定聊天
    public void sendMessage(String username, Long chatId, Object message) {
        Map<Long, SseEmitter> chatEmitters = userEmitters.get(username);
        if (chatEmitters != null) {
            SseEmitter emitter = chatEmitters.get(chatId);
            if (emitter != null) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data(message));
                } catch (IOException e) {
                    removeConnection(username, chatId);
                }
            }
        }
    }

    // 移除连接
    private void removeConnection(String username, Long chatId) {
        Map<Long, SseEmitter> chatEmitters = userEmitters.get(username);
        if (chatEmitters != null) {
            chatEmitters.remove(chatId);
            if (chatEmitters.isEmpty()) {
                userEmitters.remove(username);
            }
        }
    }
}