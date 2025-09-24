package com.aichat.roleplay.service;

import com.aichat.roleplay.mapper.ChatMapper;
import com.aichat.roleplay.mapper.MessageMapper;
import com.aichat.roleplay.mapper.RoleMapper;
import com.aichat.roleplay.model.Chat;
import com.aichat.roleplay.model.Message;
import com.aichat.roleplay.model.Role;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseService {
    @Autowired
    private  IAiChatService aiChatService;
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private ChatMapper chatMapper;
    @Resource
    private MessageMapper messageMapper;



//    // 存储每个用户的SSE连接
//    private final Map<String, Map<Long, SseEmitter>> userEmitters = new ConcurrentHashMap<>();
//
//    // 创建SSE连接
//    public SseEmitter createConnection(String username, Long chatId) {
//        // 设置超时时间（30分钟）
//        SseEmitter emitter = new SseEmitter(1800000L);
//
//        // 清理资源的回调
//        emitter.onCompletion(() -> removeConnection(username, chatId));
//        emitter.onTimeout(() -> removeConnection(username, chatId));
//        emitter.onError(e -> removeConnection(username, chatId));
//
//        // 存储连接
//        userEmitters.computeIfAbsent(username, k -> new ConcurrentHashMap<>())
//                .put(chatId, emitter);
//
//        return emitter;
//    }
//
//    // 发送消息到特定聊天
//    public void sendMessage(String username, Long chatId, Object message) {
//        Map<Long, SseEmitter> chatEmitters = userEmitters.get(username);
//        if (chatEmitters != null) {
//            SseEmitter emitter = chatEmitters.get(chatId);
//            if (emitter != null) {
//                try {
//                    emitter.send(SseEmitter.event()
//                            .name("message")
//                            .data(message));
//                } catch (IOException e) {
//                    removeConnection(username, chatId);
//                }
//            }
//        }
//    }
//
//    // 移除连接
//    private void removeConnection(String username, Long chatId) {
//        Map<Long, SseEmitter> chatEmitters = userEmitters.get(username);
//        if (chatEmitters != null) {
//            chatEmitters.remove(chatId);
//            if (chatEmitters.isEmpty()) {
//                userEmitters.remove(username);
//            }
//        }
//    }



    public SseEmitter stream(Long chatId, Long roleId, String userMessage) {
        SseEmitter emitter = new SseEmitter();

        // 1. 保存用户消息
        Message userMsg = new Message(chatId, roleId, "user", userMessage);
        messageMapper.insert(userMsg);

        // 2. 获取角色 Prompt
        Role role = roleMapper.findById(roleId);
        if(role == null){
            throw new RuntimeException("角色不存在");
        }
        String rolePrompt = role.getCharacterPrompt();

        // 3. 累积 AI 回复
        StringBuilder aiAnswer = new StringBuilder();

        // 4. 调用 AI 流式输出
        aiChatService.generateStreamResponse(rolePrompt, userMessage, token -> {
            try {
                emitter.send("data: " + token + "\n\n");
                if ("[DONE]".equals(token)) {
                    // 回复完成，保存到数据库
                    Message aiMsg = new Message(chatId, roleId, "ai", aiAnswer.toString());
                    messageMapper.insert(aiMsg);
                    emitter.complete();
                } else {
                    aiAnswer.append(token);
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;

    }
}