package com.aichat.roleplay.service;

import com.aichat.roleplay.mapper.ChatMapper;
import com.aichat.roleplay.mapper.MessageMapper;
import com.aichat.roleplay.mapper.RoleMapper;
import com.aichat.roleplay.model.Message;
import com.aichat.roleplay.model.Role;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Service
public class SseService {
    private static final Logger log = LoggerFactory.getLogger(SseService.class);
    
    @Autowired
    private IAiChatService aiChatService;

    @Autowired
    private IMessageService messageService;
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private ChatMapper chatMapper;
    @Resource
    private MessageMapper messageMapper;

    public SseEmitter stream(Long chatId, Long roleId, String userMessage) {
        log.info("SSE流式对话请求 - chatId: {}, roleId: {}, userMessage: {}", chatId, roleId, userMessage.substring(0, Math.min(50, userMessage.length())));
        
        // 验证参数类型
        log.debug("参数类型 - chatId: {}, roleId: {}", chatId.getClass().getName(), roleId.getClass().getName());
        
        SseEmitter emitter = new SseEmitter();

        try {
            List<Message> chatHistory = messageService.findByChatId(chatId);

            // 拼接上下文
            String context = buildChatContext(chatHistory, userMessage);

            // 1. 保存用户消息 - 使用Builder模式明确指定字段
            Message userMsg = Message.builder()
                    .chatId(chatId)
                    .roleId(roleId)
                    .senderType("user")
                    .content(userMessage)
                    .build();
            
            log.debug("准备插入用户消息: {}", userMsg);
            messageMapper.insert(userMsg);
            log.info("用户消息插入成功，ID: {}", userMsg.getId());



            // 2. 获取角色 Prompt
            Role role = roleMapper.findById(roleId);
            if(role == null){
                throw new RuntimeException("角色不存在");
            }
            String rolePrompt = role.getCharacterPrompt();

            // 3. 累积 AI 回复
            StringBuilder aiAnswer = new StringBuilder();

            // 4. 调用 AI 流式输出
            aiChatService.generateStreamResponse(rolePrompt, context, token -> {
                try {
                    log.debug("SSE发送数据: {}", token);
                    emitter.send("data: " + token + "\n\n");
                    if ("[DONE]".equals(token)) {
                        // 回复完成，保存到数据库
                        Message aiMsg = Message.builder()
                                .chatId(chatId)
                                .roleId(roleId)
                                .senderType("ai")
                                .content(aiAnswer.toString())
                                .build();
                        
                        log.debug("准备插入AI消息: {}", aiMsg);
                        messageMapper.insert(aiMsg);
                        log.info("AI消息插入成功，ID: {}", aiMsg.getId());
                        
                        emitter.complete();
                    } else {
                        aiAnswer.append(token);
                    }
                } catch (Exception e) {
                    log.error("SSE发送失败", e);
                    emitter.completeWithError(e);
                }
            });
        } catch (Exception e) {
            log.error("SSE处理失败", e);
            try {
                emitter.send("data: [ERROR] " + e.getMessage() + "\n\n");
                emitter.completeWithError(e);
            } catch (Exception sendEx) {
                log.error("错误消息发送失败", sendEx);
            }
        }

        return emitter;
    }
    /**
     * 拼接聊天上下文（历史消息 + 最新用户消息）
     */
    private String buildChatContext(List<Message> history, String userMessage) {
        StringBuilder context = new StringBuilder();
        if (history != null) {
            for (Message msg : history) {
                context.append("[").append(msg.getSenderType()).append("]: ")
                        .append(msg.getContent()).append("\n");
            }
        }
        // 拼接最新的用户输入
        context.append("[user]: ").append(userMessage).append("\n");
        return context.toString();
    }
}