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

import java.util.List;
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

    // 主入口：SSE流式对话
    public SseEmitter stream(Long chatId, Long roleId, String userMessage) {
        log.info("SSE流式对话 - chatId:{}, roleId:{}", chatId, roleId);
        SseEmitter emitter = new SseEmitter(60000L);
        
        try {
            processStreamRequest(chatId, roleId, userMessage, 0, emitter, null, true);
        } catch (Exception e) {
            log.error("SSE处理失败", e);
            handleStreamError(emitter, e);
        }
        
        return emitter;
    }

    // 同步获取AI响应文本（用于语音服务）
    public String getAiResponseText(Long chatId, Long roleId, String userMessage, boolean saveMessages) {
        log.info("同步获取AI响应文本 - chatId:{}, roleId:{}, saveMessages:{}", chatId, roleId, saveMessages);
        
        try {
            CompletableFuture<String> responseFuture = new CompletableFuture<>();
            processStreamRequest(chatId, roleId, userMessage, 0, null, responseFuture, saveMessages);
            
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
            
            if (rolePrompt != null && !rolePrompt.isEmpty()) {
                promptBuilder.append("你是一个AI角色扮演助手。请严格按照以下角色设定进行回复：\n");
                promptBuilder.append(rolePrompt).append("\n\n");
            }
            
            if (chatHistory != null && !chatHistory.isEmpty()) {
                promptBuilder.append("聊天历史：\n");
                promptBuilder.append(chatHistory).append("\n\n");
            }
            
            promptBuilder.append("用户说：").append(userMessage).append("\n\n");
            promptBuilder.append("请以角色的身份回复（不要说你是AI或角色扮演，直接以角色身份回应）：");
            
            String fullPrompt = promptBuilder.toString();
            log.debug("多角色聊天构建的完整提示词: {}", fullPrompt);
            
            StringBuilder aiAnswer = new StringBuilder();
            
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
                        aiAnswer.append(token);
                    }
                } catch (Exception e) {
                    log.error("处理多角色响应token失败", e);
                    responseFuture.completeExceptionally(e);
                }
            });
            
            return responseFuture.get(30, TimeUnit.SECONDS);
            
        } catch (Exception e) {
            log.error("多角色聊天生成响应失败", e);
            return "抱歉，生成响应时出现了问题。";
        }
    }

    // 统一的流式请求处理方法
    private void processStreamRequest(Long chatId, Long roleId, String originalUserMessage, int retryCount, 
                                    SseEmitter emitter, CompletableFuture<String> responseFuture, boolean saveMessages) {
        try {
            String actualUserMessage = extractActualUserMessage(originalUserMessage);
            List<Message> chatHistory = messageService.findByChatId(chatId);

            // 保存用户消息（仅在第一次调用且需要保存时）
            if (retryCount == 0 && saveMessages) {
                Message userMsg = Message.builder()
                        .chatId(chatId).roleId(roleId)
                        .senderType("user").content(actualUserMessage).build();
                messageMapper.insert(userMsg);
            }

            Role role = roleMapper.findById(roleId);
            if (role == null) {
                String errorMsg = "角色不存在";
                handleProcessingError(emitter, responseFuture, new RuntimeException(errorMsg));
                return;
            }

            StringBuilder aiAnswer = new StringBuilder();
            String optimizedPrompt = rolePromptEngineering.buildOptimizedPrompt(role, actualUserMessage, buildChatHistory(chatHistory));

            aiChatService.generateStreamResponseDirect(optimizedPrompt, token -> {
                try {
                    if ("[DONE]".equals(token)) {
                        String cleanedResponse = cleanAiResponse(aiAnswer.toString(), actualUserMessage);
                        
                        // 发送完成标记（仅SSE模式）
                        if (emitter != null) {
                            emitter.send("data: " + token + "\n\n");
                        }
                        
                        // 统一的反思处理
                        handleResponseWithReflection(chatId, roleId, actualUserMessage, cleanedResponse, 
                                                   retryCount, emitter, responseFuture, saveMessages);
                    } else if (!"[ERROR]".equals(token)) {
                        aiAnswer.append(token);
                        
                        // 发送token（仅SSE模式）
                        if (emitter != null) {
                            emitter.send("data: " + token + "\n\n");
                        }
                    } else {
                        handleProcessingError(emitter, responseFuture, new RuntimeException("AI回复错误"));
                    }
                } catch (Exception e) {
                    log.error("处理AI响应token失败", e);
                    handleProcessingError(emitter, responseFuture, e);
                }
            });

        } catch (Exception e) {
            log.error("处理流式请求失败", e);
            handleProcessingError(emitter, responseFuture, e);
        }
    }

    // 统一的反思处理方法
    private void handleResponseWithReflection(Long chatId, Long roleId, String originalQuery, String aiResponse, 
                                            int retryCount, SseEmitter emitter, CompletableFuture<String> responseFuture, boolean saveMessages) {
        try {
            ReflectionResult result = reflectionAgentService.reflect(originalQuery, aiResponse, chatId, roleId, retryCount);

            if (result.needsRetry()) {
                if (result.getRetryCount() >= 3) {
                    log.warn("达到最大重试次数，返回当前响应");
                    finishProcessing(chatId, roleId, aiResponse, emitter, responseFuture, saveMessages, "[ERROR] 达到最大重试次数");
                    return;
                }

                // 记录反思日志
                Long userId = getCurrentUserId(chatId);
                reflectionLogService.saveReflectionLog(chatId, roleId, userId, originalQuery, aiResponse, result, 0);

                log.info("反思建议重试，执行第{}次重试", result.getRetryCount());
                
                // 发送重试提示（仅SSE模式）
                if (emitter != null) {
                    emitter.send("data: [RETRY] 重新生成中...\n\n");
                }
                
                // 递归重试
                processStreamRequest(chatId, roleId, result.getRegeneratedQuery(), result.getRetryCount(), 
                                   emitter, responseFuture, saveMessages);

            } else if (result.isSuccess()) {
                finishProcessing(chatId, roleId, aiResponse, emitter, responseFuture, saveMessages, null);
            } else {
                // 记录反思日志
                Long userId = getCurrentUserId(chatId);
                reflectionLogService.saveReflectionLog(chatId, roleId, userId, originalQuery, aiResponse, result, 0);
                
                log.warn("反思检测到问题但不重试: {}", result.getErrorMessage());
                finishProcessing(chatId, roleId, aiResponse, emitter, responseFuture, saveMessages, result.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("反思处理异常", e);
            // 即使反思失败，也返回AI响应
            finishProcessing(chatId, roleId, aiResponse, emitter, responseFuture, saveMessages, null);
        }
    }

    // 统一的完成处理方法
    private void finishProcessing(Long chatId, Long roleId, String aiResponse, SseEmitter emitter, 
                                CompletableFuture<String> responseFuture, boolean saveMessages, String errorMessage) {
        try {
            // 保存AI消息
            if (saveMessages) {
                saveAiMessage(chatId, roleId, aiResponse);
            }

            // 处理SSE响应
            if (emitter != null) {
                if (errorMessage != null) {
                    emitter.send("data: [ERROR] " + errorMessage + "\n\n");
                }
                emitter.complete();
            }

            // 处理同步响应
            if (responseFuture != null) {
                responseFuture.complete(aiResponse);
            }
        } catch (Exception e) {
            log.error("完成处理失败", e);
            handleProcessingError(emitter, responseFuture, e);
        }
    }

    // 统一的错误处理方法
    private void handleProcessingError(SseEmitter emitter, CompletableFuture<String> responseFuture, Exception e) {
        if (emitter != null) {
            handleStreamError(emitter, e);
        }
        if (responseFuture != null) {
            responseFuture.completeExceptionally(e);
        }
    }

    // SSE错误处理
    private void handleStreamError(SseEmitter emitter, Exception e) {
        try {
            emitter.send("data: [ERROR] " + e.getMessage() + "\n\n");
            emitter.completeWithError(e);
        } catch (Exception sendEx) {
            log.error("发送错误失败", sendEx);
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

    // 获取当前用户ID
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