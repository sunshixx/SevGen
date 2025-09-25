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