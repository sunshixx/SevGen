package com.aichat.roleplay.service.impl;

import com.aichat.roleplay.dto.RoleSelectionResult;
import com.aichat.roleplay.mapper.RoleMapper;
import com.aichat.roleplay.model.Chat;
import com.aichat.roleplay.model.Role;
import com.aichat.roleplay.service.IChatService;
import com.aichat.roleplay.service.IChatroomCollaborationService;
import com.aichat.roleplay.service.IRoleSelector;
import com.aichat.roleplay.service.SseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 聊天室协作服务实现
 * 在现有聊天室内实现多角色协作功能
 */
@Service
public class ChatroomCollaborationServiceImpl implements IChatroomCollaborationService {
    
    private static final Logger log = LoggerFactory.getLogger(ChatroomCollaborationServiceImpl.class);
    
    @Autowired
    private IRoleSelector roleSelector;
    
    @Autowired
    private SseService sseService;
    
    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private IChatService chatService;
    
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    @Override
    public SseEmitter handleCollaborativeMessage(Long chatId, String userMessage, String context) {
        log.info("处理聊天室协作消息，聊天ID: {}, 消息: {}", chatId, userMessage);
        
        // 验证聊天室是否存在
        Optional<Chat> chatOpt = chatService.getChatById(chatId);
        if (!chatOpt.isPresent()) {
            throw new RuntimeException("聊天室不存在: " + chatId);
        }
        
        Chat chat = chatOpt.get();
        Long userId = chat.getUserId();
        
        SseEmitter emitter = new SseEmitter(300000L); // 30秒超时
        
        // 异步处理
        executorService.submit(() -> {
            try {
                // 1. 使用LLM选择角色
                log.info("开始角色选择，用户ID: {}, 消息: {}", userId, userMessage);
                RoleSelectionResult selectionResult = roleSelector.selectRoles(userId, userMessage, context);
                
                // 2. 发送角色选择结果（JSON格式，不带内部分隔符）
                emitter.send("data: " + selectionResult.toString() + "\n\n");
                
                // 3. 执行角色响应
                executeParallelResponses(chatId, userMessage, selectionResult, emitter);
                
            } catch (Exception e) {
                log.error("处理协作消息时发生错误", e);
                try {
                    emitter.send("data: {\"type\":\"ERROR\",\"message\":\"" + e.getMessage() + "\"}\n\n");
                    emitter.completeWithError(e);
                } catch (Exception sendEx) {
                    log.error("发送错误消息失败", sendEx);
                    emitter.completeWithError(sendEx);
                }
            }
        });
        
        return emitter;
    }
    
    /**
     * 并行执行多个角色的响应 - 按order顺序异步返回
     */
    private void executeParallelResponses(Long chatId, String userMessage, RoleSelectionResult selectionResult, SseEmitter emitter) {
        log.info("开始并行执行 {} 个角色的响应", selectionResult.getRoles().size());
        
        // 创建CompletableFuture列表来跟踪所有角色的执行状态
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        // 为每个角色创建异步任务，按order排序
        List<RoleSelectionResult.RoleSelection> sortedRoles = selectionResult.getRoles();
        // 如果RoleSelection有order字段，可以在这里排序
        // sortedRoles.sort(Comparator.comparing(RoleSelectionResult.RoleSelection::getOrder));
        
        for (int i = 0; i < sortedRoles.size(); i++) {
            final int order = i;
            final RoleSelectionResult.RoleSelection selection = sortedRoles.get(i);
            
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                executeRoleResponseAsync(chatId, userMessage, selection, emitter, order);
            }, executorService);
            futures.add(future);
        }
        
        // 异步等待所有角色完成，然后发送最终完成信号
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .whenComplete((result, throwable) -> {
                try {
                    if (throwable != null) {
                        log.error("部分角色响应失败", throwable);
                        emitter.send("data: {\"type\":\"COLLABORATION_ERROR\",\"message\":\"部分角色响应失败\"}\n\n");
                    } else {
                        log.info("所有角色响应完成");
                    }
                    emitter.send("data: {\"type\":\"COLLABORATION_END\",\"message\":\"所有角色回答完成\"}\n\n");
                    emitter.complete();
                } catch (Exception e) {
                    log.error("发送协作完成信号失败", e);
                    emitter.completeWithError(e);
                }
            });
    }
    
    /**
     * 异步执行单个角色的响应 - 返回JSON格式数据
     */
    private void executeRoleResponseAsync(Long chatId, String userMessage, RoleSelectionResult.RoleSelection selection, SseEmitter emitter, int order) {
        try {
            log.info("开始执行角色响应: {}, order: {}", selection.getRoleName(), order);
            
            // 根据角色名称查找角色ID
            List<Role> roles = roleMapper.findByNameContaining(selection.getRoleName());
            Role role = roles.stream()
                    .filter(r -> selection.getRoleName().equals(r.getName()))
                    .findFirst()
                    .orElse(null);
            
            if (role == null) {
                log.warn("未找到角色: {}", selection.getRoleName());
                emitter.send("data: {\"type\":\"ROLE_ERROR\",\"roleName\":\"" + selection.getRoleName() + "\",\"order\":" + order + ",\"message\":\"未找到角色\"}\n\n");
                return;
            }
            
            // 发送角色开始响应信号
            emitter.send("data: {\"type\":\"ROLE_START\",\"roleName\":\"" + selection.getRoleName() + "\",\"order\":" + order + ",\"message\":\"开始回答...\"}\n\n");
            
            // 构建角色特定的消息
            String roleMessage = selection.getCustomPrompt() != null ? 
                    selection.getCustomPrompt() + "\n\n用户问题: " + userMessage : 
                    userMessage;
            
            // 获取AI响应文本 - 不保存消息，避免重复保存
            String aiResponse = sseService.getAiResponseText(chatId, role.getId(), roleMessage, false);
            
            // 发送角色响应内容
            emitter.send("data: {\"type\":\"ROLE_RESPONSE\",\"roleName\":\"" + selection.getRoleName() + "\",\"roleId\":" + role.getId() + ",\"order\":" + order + ",\"content\":\"" + escapeJson(aiResponse) + "\"}\n\n");
            
            // 发送角色完成信号
            emitter.send("data: {\"type\":\"ROLE_END\",\"roleName\":\"" + selection.getRoleName() + "\",\"order\":" + order + ",\"message\":\"回答完成\"}\n\n");
            
            log.info("角色响应完成: {}, order: {}", selection.getRoleName(), order);
            
        } catch (Exception e) {
            log.error("角色 {} 响应失败, order: {}", selection.getRoleName(), order, e);
            try {
                emitter.send("data: {\"type\":\"ROLE_ERROR\",\"roleName\":\"" + selection.getRoleName() + "\",\"order\":" + order + ",\"message\":\"" + escapeJson(e.getMessage()) + "\"}\n\n");
            } catch (Exception sendEx) {
                log.error("发送角色错误信号失败", sendEx);
            }
        }
    }
    
    @Override
    public Map<String, String> handleCollaborativeMessageSync(Long chatId, String userMessage, String context) {
        log.info("同步处理聊天室协作消息，聊天ID: {}, 消息: {}", chatId, userMessage);
        
        // 验证聊天室是否存在
        Optional<Chat> chatOpt = chatService.getChatById(chatId);
        if (!chatOpt.isPresent()) {
            throw new RuntimeException("聊天室不存在: " + chatId);
        }
        
        Chat chat = chatOpt.get();
        Long userId = chat.getUserId();
        
        try {
            // 1. 使用LLM选择角色
            log.info("开始角色选择，用户ID: {}, 消息: {}", userId, userMessage);
            RoleSelectionResult selectionResult = roleSelector.selectRoles(userId, userMessage, context);
            
            // 2. 同步执行角色响应
            Map<String, String> responses = new ConcurrentHashMap<>();
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            
            for (RoleSelectionResult.RoleSelection selection : selectionResult.getRoles()) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        // 根据角色名称查找角色ID
                        List<Role> roles = roleMapper.findByNameContaining(selection.getRoleName());
                        Role role = roles.stream()
                                .filter(r -> selection.getRoleName().equals(r.getName()))
                                .findFirst()
                                .orElse(null);
                        
                        if (role == null) {
                            log.warn("未找到角色: {}", selection.getRoleName());
                            responses.put(selection.getRoleName(), "错误：未找到角色");
                            return;
                        }
                        
                        // 构建角色特定的消息
                        String roleMessage = selection.getCustomPrompt() != null ? 
                                selection.getCustomPrompt() + "\n\n用户问题: " + userMessage : 
                                userMessage;
                        
                        // 获取AI响应文本 - 不保存消息，避免重复保存
                        String aiResponse = sseService.getAiResponseText(chatId, role.getId(), roleMessage, false);
                        responses.put(selection.getRoleName(), aiResponse);
                        
                        log.info("角色响应完成: {}", selection.getRoleName());
                        
                    } catch (Exception e) {
                        log.error("角色 {} 响应失败", selection.getRoleName(), e);
                        responses.put(selection.getRoleName(), "错误：" + e.getMessage());
                    }
                }, executorService);
                futures.add(future);
            }
            
            // 等待所有角色完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            log.info("所有角色响应完成，共 {} 个角色", responses.size());
            return responses;
            
        } catch (Exception e) {
            log.error("处理协作消息时发生错误", e);
            throw new RuntimeException("处理协作消息失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 转义JSON字符串中的特殊字符
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId(Long chatId) {
        Optional<Chat> chatOpt = chatService.getChatById(chatId);
        return chatOpt.map(Chat::getUserId).orElse(null);
    }
    

}