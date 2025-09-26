package com.aichat.roleplay.service;

import com.aichat.roleplay.mapper.ChatMapper;
import com.aichat.roleplay.mapper.MessageMapper;
import com.aichat.roleplay.mapper.RoleMapper;
import com.aichat.roleplay.model.Message;
import com.aichat.roleplay.model.ReflectionResult;
import com.aichat.roleplay.model.Role;
import com.aichat.roleplay.util.RolePromptEngineering;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SseService {
    private static final Logger log = LoggerFactory.getLogger(SseService.class);

    @Autowired private IAiChatService aiChatService;
    @Autowired private IMessageService messageService;
    @Autowired private IReflectionAgentService reflectionAgentService;
    @Autowired private IReflectionLogService reflectionLogService;
    @Autowired private RolePromptEngineering rolePromptEngineering;

    @Resource private RoleMapper roleMapper;
    @Resource private ChatMapper chatMapper;
    @Resource private MessageMapper messageMapper;

    public SseEmitter stream(Long chatId, Long roleId, String userMessage) {
        return streamWithReflection(chatId, roleId, userMessage, 0);
    }

    // 主流式对话入口
    private SseEmitter streamWithReflection(Long chatId, Long roleId, String originalUserMessage, int currentRetryCount) {
        log.info("SSE流式对话 - chatId:{}, roleId:{}, retry:{}", chatId, roleId, currentRetryCount);
        SseEmitter emitter = new SseEmitter(60000L);

        try {
            String actualUserMessage = extractActualUserMessage(originalUserMessage);
            List<Message> chatHistory = messageService.findByChatId(chatId);

            Message userMsg = Message.builder()
                    .chatId(chatId).roleId(roleId)
                    .senderType("user").content(actualUserMessage).build();
            messageMapper.insert(userMsg);

            Role role = roleMapper.findById(roleId);
            if(role == null) throw new RuntimeException("角色不存在");

            StringBuilder aiAnswer = new StringBuilder();
            String optimizedPrompt = rolePromptEngineering.buildOptimizedPrompt(role, actualUserMessage, buildChatHistory(chatHistory));

            aiChatService.generateStreamResponseDirect(optimizedPrompt, token -> {
                try {
                    log.debug("SSE发送数据: {}", token);
                    if ("[DONE]".equals(token)) {
                        String cleanedResponse = cleanAiResponse(aiAnswer.toString(), actualUserMessage);
                        emitter.send("data: " + token + "\n\n");
                        handleAiResponseComplete(emitter, chatId, roleId, actualUserMessage, cleanedResponse, currentRetryCount);
                    } else if (!"[ERROR]".equals(token)) {
                        aiAnswer.append(token);
                        emitter.send("data: " + token + "\n\n");
                    } else {
                        emitter.completeWithError(new RuntimeException("AI回复错误"));
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
                log.error("发送错误失败", sendEx);
            }
        }
        return emitter;
    }

    // 反思分析处理
    private void handleAiResponseComplete(SseEmitter emitter, Long chatId, Long roleId, String originalQuery, String aiResponse, int retryCount) {
        try {
            ReflectionResult result = reflectionAgentService.reflect(originalQuery, aiResponse, chatId, roleId, retryCount);

            if (result.needsRetry()) {
                if (result.getRetryCount() >= 3) {
                    saveAiMessage(chatId, roleId, aiResponse);
                    emitter.send("data: [ERROR] 达到最大重试次数\n\n");
                    emitter.complete();
                    return;
                }
                Long userId = getCurrentUserId(chatId);
                reflectionLogService.saveReflectionLog(chatId, roleId, userId, originalQuery, aiResponse, result, 0);

                emitter.send("data: [RETRY] 重新生成中...\n\n");
                performRetry(emitter, chatId, roleId, result.getRegeneratedQuery(), result.getRetryCount());

            } else if (result.isSuccess()) {
                saveAiMessage(chatId, roleId, aiResponse);
                emitter.complete();
            } else {
                Long userId = getCurrentUserId(chatId);
                reflectionLogService.saveReflectionLog(chatId, roleId, userId, originalQuery, aiResponse, result, 0);
                saveAiMessage(chatId, roleId, aiResponse);
                emitter.send("data: [ERROR] " + result.getErrorMessage() + "\n\n");
                emitter.complete();
            }
        } catch (Exception e) {
            log.error("反思处理异常", e);
            emitter.completeWithError(e);
        }
    }

    // 保存AI消息
    private void saveAiMessage(Long chatId, Long roleId, String aiResponse) {
        try {
            Message aiMsg = Message.builder()
                    .chatId(chatId).roleId(roleId)
                    .senderType("ai").content(aiResponse).build();
            messageMapper.insert(aiMsg);
        } catch (Exception e) {
            log.error("保存AI消息失败", e);
        }
    }

    // 执行重试
    private void performRetry(SseEmitter emitter, Long chatId, Long roleId, String retryQuery, int retryCount) {
        try {
            String cleanedQuery = extractActualUserMessage(retryQuery);
            List<Message> chatHistory = messageService.findByChatId(chatId);
            Role role = roleMapper.findById(roleId);

            StringBuilder newAiAnswer = new StringBuilder();
            String optimizedPrompt = rolePromptEngineering.buildOptimizedPrompt(role, cleanedQuery, buildChatHistory(chatHistory));

            aiChatService.generateStreamResponseDirect(optimizedPrompt, token -> {
                try {
                    if ("[DONE]".equals(token)) {
                        String cleanedResponse = cleanAiResponse(newAiAnswer.toString(), cleanedQuery);
                        emitter.send("data: " + token + "\n\n");
                        handleAiResponseComplete(emitter, chatId, roleId, cleanedQuery, cleanedResponse, retryCount);
                    } else if (!"[ERROR]".equals(token)) {
                        newAiAnswer.append(token);
                        emitter.send("data: " + token + "\n\n");
                    } else {
                        emitter.completeWithError(new RuntimeException("重试失败"));
                    }
                } catch (Exception e) {
                    emitter.completeWithError(e);
                }
            });
        } catch (Exception e) {
            log.error("重试失败", e);
            emitter.completeWithError(e);
        }
    }

    private Long getCurrentUserId(Long chatId) {
        try {
            return chatMapper.selectById(chatId).getUserId();
        } catch (Exception e) {
            return null;
        }
    }

    // 提取真实用户消息
    private String extractActualUserMessage(String message) {
        String cleaned = message;
        if (cleaned.startsWith("RETRY")) {
            int spaceIndex = cleaned.indexOf(' ');
            if (spaceIndex > 0) cleaned = cleaned.substring(spaceIndex + 1);
        }
        return removeAccumulatedPrompts(cleaned);
    }

    // 移除累积的提示词
    private String removeAccumulatedPrompts(String message) {
        return message.replaceAll("\\s*\\(请用更.*?回答\\)\\s*", " ")
                     .replaceAll("\\s*请用更.*?回答\\s*", " ")
                     .replaceAll("\\s+", " ").trim();
    }

    // 构建聊天历史（用于 prompt）
    private String buildChatHistory(List<Message> history) {
        if (history == null || history.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        for (Message msg : history) {
            String sender = "ai".equals(msg.getSenderType()) ? "AI" : "用户";
            sb.append(sender).append(": ").append(msg.getContent()).append("\n");
        }
        return sb.toString().trim();
    }

    // 清理AI回复
    private String cleanAiResponse(String aiResponse, String userMessage) {
        return aiResponse.replaceAll("请用更清晰的方式回答", "")
                        .replaceAll("\\(请用更.*?\\)", "")
                        .replaceAll("用户问题：.*?\\n", "")
                        .trim();
    }
}