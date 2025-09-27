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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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

    // 同步获取AI响应文本（用于语音服务）- 包含完整的prompt和反思逻辑
    public String getAiResponseText(Long chatId, Long roleId, String userMessage) {
        return getAiResponseText(chatId, roleId, userMessage, true);
    }
    
    // 同步获取AI响应文本（用于语音服务）- 包含完整的prompt和反思逻辑，可控制是否保存消息
    public String getAiResponseText(Long chatId, Long roleId, String userMessage, boolean saveMessages) {
        log.info("同步获取AI响应文本 - chatId:{}, roleId:{}, saveMessages:{}", chatId, roleId, saveMessages);
        
        try {
            CompletableFuture<String> responseFuture = new CompletableFuture<>();
            processWithReflectionSync(chatId, roleId, userMessage, 0, responseFuture, saveMessages);
            
            // 等待响应完成，最多30秒（优化超时时间）
            return responseFuture.get(30, TimeUnit.SECONDS);
            
        } catch (Exception e) {
            log.error("同步获取AI响应失败", e);
            throw new RuntimeException("获取AI响应失败: " + e.getMessage(), e);
        }
    }

    // 多角色聊天专用：根据角色提示词和用户消息生成响应（不保存到数据库）
    public String generateRoleResponse(String rolePrompt, String userMessage, String chatHistory) {
        log.info("多角色聊天生成响应 - rolePrompt长度:{}, userMessage长度:{}", 
                rolePrompt != null ? rolePrompt.length() : 0, 
                userMessage != null ? userMessage.length() : 0);
        
        try {
            CompletableFuture<String> responseFuture = new CompletableFuture<>();
            
            // 构建完整的提示词
            StringBuilder promptBuilder = new StringBuilder();
            
            // 添加角色设定
            if (rolePrompt != null && !rolePrompt.isEmpty()) {
                promptBuilder.append("你是一个AI角色扮演助手。请严格按照以下角色设定进行回复：\n");
                promptBuilder.append(rolePrompt).append("\n\n");
            }
            
            // 添加聊天历史
            if (chatHistory != null && !chatHistory.isEmpty()) {
                promptBuilder.append("聊天历史：\n");
                promptBuilder.append(chatHistory).append("\n\n");
            }
            
            // 添加用户消息
            promptBuilder.append("用户说：").append(userMessage).append("\n\n");
            promptBuilder.append("请以角色的身份回复（不要说你是AI或角色扮演，直接以角色身份回应）：");
            
            String fullPrompt = promptBuilder.toString();
            log.debug("多角色聊天构建的完整提示词: {}", fullPrompt);
            
            StringBuilder aiAnswer = new StringBuilder();
            
            // 使用现有的流式响应机制
            aiChatService.generateStreamResponseDirect(fullPrompt, token -> {
                try {
                    if ("[DONE]".equals(token)) {
                        String finalResponse = aiAnswer.toString().trim();
                        log.debug("多角色响应生成完成，长度: {}", finalResponse.length());
                        responseFuture.complete(finalResponse.isEmpty() ? "抱歉，我现在无法回应。" : finalResponse);
                    } else if ("[ERROR]".equals(token)) {
                        log.error("多角色响应生成出错");
                        responseFuture.completeExceptionally(new RuntimeException("生成响应时出现错误"));
                    } else {
                        // 正常的token，累积到响应中
                        aiAnswer.append(token);
                    }
                } catch (Exception e) {
                    log.error("处理多角色响应token失败", e);
                    responseFuture.completeExceptionally(e);
                }
            });
            
            // 等待响应完成，最多等待30秒
            return responseFuture.get(30, TimeUnit.SECONDS);
            
        } catch (Exception e) {
            log.error("多角色聊天生成响应失败", e);
            return "抱歉，生成响应时出现了问题。";
        }
    }

    // 同步处理带反思逻辑的AI响应
    private void processWithReflectionSync(Long chatId, Long roleId, String originalUserMessage, int currentRetryCount, CompletableFuture<String> responseFuture) {
        processWithReflectionSync(chatId, roleId, originalUserMessage, currentRetryCount, responseFuture, true);
    }
    
    // 同步处理带反思逻辑的AI响应，可控制是否保存消息
    private void processWithReflectionSync(Long chatId, Long roleId, String originalUserMessage, int currentRetryCount, CompletableFuture<String> responseFuture, boolean saveMessages) {
        try {
            String actualUserMessage = extractActualUserMessage(originalUserMessage);
            List<Message> chatHistory = messageService.findByChatId(chatId);

            // 只在第一次调用时保存用户消息，且saveMessages为true时
            if (currentRetryCount == 0 && saveMessages) {
                Message userMsg = Message.builder()
                        .chatId(chatId).roleId(roleId)
                        .senderType("user").content(actualUserMessage).build();
                messageMapper.insert(userMsg);
            }

            Role role = roleMapper.findById(roleId);
            if(role == null) {
                responseFuture.completeExceptionally(new RuntimeException("角色不存在"));
                return;
            }

            StringBuilder aiAnswer = new StringBuilder();
            String optimizedPrompt = rolePromptEngineering.buildOptimizedPrompt(role, actualUserMessage, buildChatHistory(chatHistory));

            aiChatService.generateStreamResponseDirect(optimizedPrompt, token -> {
                try {
                    if ("[DONE]".equals(token)) {
                        String cleanedResponse = cleanAiResponse(aiAnswer.toString(), actualUserMessage);
                        handleAiResponseCompleteSync(chatId, roleId, actualUserMessage, cleanedResponse, currentRetryCount, responseFuture, saveMessages);
                    } else if (!"[ERROR]".equals(token)) {
                        aiAnswer.append(token);
                    } else {
                        responseFuture.completeExceptionally(new RuntimeException("AI回复错误"));
                    }
                } catch (Exception e) {
                    log.error("处理AI响应失败", e);
                    responseFuture.completeExceptionally(e);
                }
            });

        } catch (Exception e) {
            log.error("同步处理失败", e);
            responseFuture.completeExceptionally(e);
        }
    }

    // 同步版本的反思分析处理
    private void handleAiResponseCompleteSync(Long chatId, Long roleId, String originalQuery, String aiResponse, int retryCount, CompletableFuture<String> responseFuture) {
        handleAiResponseCompleteSync(chatId, roleId, originalQuery, aiResponse, retryCount, responseFuture, true);
    }
    
    // 同步版本的反思分析处理，可控制是否保存消息
    private void handleAiResponseCompleteSync(Long chatId, Long roleId, String originalQuery, String aiResponse, int retryCount, CompletableFuture<String> responseFuture, boolean saveMessages) {
        try {
            ReflectionResult result = reflectionAgentService.reflect(originalQuery, aiResponse, chatId, roleId, retryCount);

            if (result.needsRetry()) {
                if (result.getRetryCount() >= 3) {
                    log.warn("达到最大重试次数，返回当前响应");
                    if (saveMessages) {
                        saveAiMessage(chatId, roleId, aiResponse);
                    }
                    responseFuture.complete(aiResponse);
                    return;
                }
                
                Long userId = getCurrentUserId(chatId);
                reflectionLogService.saveReflectionLog(chatId, roleId, userId, originalQuery, aiResponse, result, 0);
                
                log.info("反思建议重试，执行第{}次重试", result.getRetryCount());
                // 递归调用进行重试
                processWithReflectionSync(chatId, roleId, result.getRegeneratedQuery(), result.getRetryCount(), responseFuture, saveMessages);

            } else if (result.isSuccess()) {
                if (saveMessages) {
                    saveAiMessage(chatId, roleId, aiResponse);
                }
                responseFuture.complete(aiResponse);
            } else {
                Long userId = getCurrentUserId(chatId);
                reflectionLogService.saveReflectionLog(chatId, roleId, userId, originalQuery, aiResponse, result, 0);
                if (saveMessages) {
                    saveAiMessage(chatId, roleId, aiResponse);
                }
                log.warn("反思检测到问题但不重试: {}", result.getErrorMessage());
                responseFuture.complete(aiResponse);
            }
        } catch (Exception e) {
            log.error("同步反思处理异常", e);
            // 即使反思失败，也返回AI响应
            if (saveMessages) {
                saveAiMessage(chatId, roleId, aiResponse);
            }
            responseFuture.complete(aiResponse);
        }
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