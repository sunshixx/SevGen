package com.aichat.roleplay.service.impl;

import cn.hutool.json.JSONUtil;
import com.aichat.roleplay.mapper.RoleMapper;
import com.aichat.roleplay.model.Role;
import com.aichat.roleplay.model.ChatRoom;
import com.aichat.roleplay.service.ChatroomCollaborationService;
import com.aichat.roleplay.service.IChatRoomService;
import com.aichat.roleplay.service.IChatroomMessageService;
import com.aichat.roleplay.service.IRoleSelector;
import com.aichat.roleplay.service.SseService;

import com.aichat.roleplay.service.IAiChatService;
import com.aichat.roleplay.util.RolePromptEngineering;
import com.aichat.roleplay.mapper.ChatroomMessageMapper;
import com.aichat.roleplay.model.ChatroomMessage;

import com.aichat.roleplay.context.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class ChatroomCollaborationServiceimpl implements ChatroomCollaborationService {

    private static final Logger log = LoggerFactory.getLogger(ChatroomCollaborationServiceimpl.class);

    @Autowired
    private IRoleSelector roleSelector;

    @Autowired
    private SseService sseService;

    @Autowired
    private IChatRoomService chatRoomService;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private IChatroomMessageService chatroomMessageService;


    @Autowired
    private RolePromptEngineering rolePromptEngineering;

    @Autowired
    private IAiChatService aiChatService;

    @Autowired
    private ChatroomMessageMapper chatroomMessageMapper;


    @Value("${chatroom.collaboration.top-k-roles:3}")
    private int topKRoles;

    @Value("${chatroom.collaboration.max-concurrent-roles:5}")
    private int maxConcurrentRoles;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    public SseEmitter handleCollaborativeMessage(Long chatRoomId, String userMessage, String context) {
        log.info("处理协作消息 - chatRoomId: {}, userMessage: {}", chatRoomId, userMessage);


        // 增加超时时间到5分钟，并添加超时和完成回调
        SseEmitter emitter = new SseEmitter(300000L);
        
        // 设置超时回调
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时 - chatRoomId: {}, 连接将被关闭", chatRoomId);
            try {
                emitter.complete();
            } catch (Exception e) {
                log.error("SSE超时完成失败", e);
            }
        });
        
        // 设置完成回调
        emitter.onCompletion(() -> {
            log.warn("SSE连接完成回调被触发 - chatRoomId: {}", chatRoomId);
            // 打印堆栈跟踪以找出是谁调用了complete
            Thread.dumpStack();
        });
        
        // 设置错误回调
        emitter.onError((throwable) -> {
            log.error("SSE连接错误回调被触发 - chatRoomId: {}", chatRoomId, throwable);
            // 打印堆栈跟踪以找出错误来源
            Thread.dumpStack();
        });

        try {
            // 在异步处理前获取当前用户ID
            Long currentUserId = UserContext.getCurrentUserId();
            log.info("当前用户ID: {}", currentUserId);
            
            // 异步处理协作消息，传递userId参数
            CompletableFuture.runAsync(() -> {
                try {
                    processCollaborativeMessage(chatRoomId, userMessage, context, emitter, currentUserId);

                } catch (Exception e) {
                    log.error("处理协作消息失败", e);
                    handleError(emitter, e);
                }
            }, executorService);

        } catch (Exception e) {
            log.error("启动协作消息处理失败", e);
            handleError(emitter, e);
        }

        return emitter;
    }

    /**
     * 处理协作消息的核心逻辑
     */

    private void processCollaborativeMessage(Long chatRoomId, String userMessage, String context, SseEmitter emitter, Long userId) {
        try {
            // 0. 保存用户消息

            if (userId != null) {
                try {
                    chatroomMessageService.saveUserMessage(chatRoomId, userId, userMessage);
                    log.info("用户消息已保存到聊天室 {}", chatRoomId);
                } catch (Exception e) {
                    log.error("保存用户消息失败", e);
                    // 不中断流程，继续处理
                }
            }

            // 1. 获取聊天室角色
            List<Role> availableRoles = getChatRoomRoles(chatRoomId);
            if (availableRoles.isEmpty()) {
                sendMessage(emitter, "ERROR", "聊天室没有可用角色", null, null);
                emitter.complete();
                return;
            }

            // 2. 选择合适的角色
            int actualTopK = Math.min(topKRoles, Math.min(availableRoles.size(), maxConcurrentRoles));
            

            // 构建通用聊天历史用于角色选择（不包含角色特定信息）
            String generalChatHistory = chatroomMessageService.buildChatHistory(chatRoomId, 10);
            String selectionContext = (context != null && !context.trim().isEmpty() ? context : "") + 
                                    (generalChatHistory.isEmpty() ? "" : "\n聊天历史:\n" + generalChatHistory);
            
            // 直接使用selectTopKRoles方法进行智能角色选择
            var selectionResult = roleSelector.selectTopKRoles(userMessage, availableRoles, actualTopK, selectionContext);

            List<Role> selectedRoles = availableRoles.stream()
                    .filter(role -> selectionResult.getSelectedRoleIds().contains(role.getId()))
                    .collect(Collectors.toList());
            
            log.info("通过selectTopKRoles选择了 {} 个角色: {}, 选择原因: {}, 置信度: {}", 
                    selectedRoles.size(),
                    selectedRoles.stream().map(Role::getName).collect(Collectors.toList()),
                    selectionResult.getReason(),
                    selectionResult.getConfidence());

            log.info("选择了 {} 个角色参与对话: {}", selectedRoles.size(),
                    selectedRoles.stream().map(Role::getName).collect(Collectors.toList()));

            // 3. 发送开始消息
            sendMessage(emitter, "START", "开始协作对话", null, null);


            // 4. 为每个角色创建异步任务，使用CompletableFuture来跟踪流式响应的完成状态
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (Role role : selectedRoles) {
                CompletableFuture<Void> future = new CompletableFuture<>();
                
                // 异步处理角色响应
                executorService.submit(() -> {
                    try {
                        log.info("开始处理角色: {}", role.getName());
                        // 为每个角色构建特定的聊天历史
                        String roleChatHistory = chatroomMessageService.buildRoleChatHistory(chatRoomId, role.getId(), 10);
                        String roleContext = (context != null && !context.trim().isEmpty() ? context : "") + 
                                           (roleChatHistory.isEmpty() ? "" : "\n聊天历史:\n" + roleChatHistory);
                        generateRoleStreamResponse(role, userMessage, roleContext, chatRoomId, emitter, future);

                    } catch (Exception e) {
                        log.error("角色 {} 流式响应失败", role.getName(), e);
                        sendMessage(emitter, "ERROR", "角色响应失败: " + e.getMessage(),
                                role.getId(), role.getName());

                        future.completeExceptionally(e);
                    }
                });


                futures.add(future);
            }


            // 5. 等待所有角色的流式响应真正完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> {
                        try {
                            log.info("所有角色流式响应真正完成，准备关闭SSE连接 - chatRoomId: {}", chatRoomId);

                            sendMessage(emitter, "COMPLETE", "协作对话完成", null, null);
                            emitter.complete();
                        } catch (Exception e) {
                            log.error("完成协作对话时出错", e);
                            emitter.completeWithError(e);
                        }
                    })
                    .exceptionally(throwable -> {
                        log.error("协作对话处理异常", throwable);
                        handleError(emitter, (Exception) throwable);
                        return null;
                    });

        } catch (Exception e) {
            log.error("处理协作消息失败", e);
            handleError(emitter, e);
        }
    }

    /**
     * 生成单个角色的流式响应
     */

    private void generateRoleStreamResponse(Role role, String userMessage, String roleContext, Long chatRoomId, SseEmitter emitter, CompletableFuture<Void> future) {

        try {
            log.info("开始生成角色 {} 的流式响应", role.getName());

            // 发送角色开始响应消息
            sendMessage(emitter, "ROLE_START", "角色开始响应", role.getId(), role.getName());

            StringBuilder responseBuilder = new StringBuilder();

            
            // 使用RolePromptEngineering构建优化的prompt
            String optimizedPrompt = rolePromptEngineering.buildOptimizedPrompt(role, userMessage, roleContext);
            
            log.debug("角色 {} 的完整prompt: {}", role.getName(), optimizedPrompt);

            // 使用标志位跟踪响应状态
            final boolean[] responseCompleted = {false};

            // 直接调用AI服务生成流式响应
            aiChatService.generateStreamResponseDirect(optimizedPrompt, token -> {
                try {
                    // 添加详细的token调试日志
                    log.info("角色 {} 收到token: '{}', 长度: {}, 类型: {}", 
                            role.getName(), token, token != null ? token.length() : 0, 
                            token != null ? token.getClass().getSimpleName() : "null");
                    
                    // 如果响应已完成或SSE连接已断开，跳过处理
                    if (responseCompleted[0]) {
                        log.info("角色 {} 跳过处理token，响应已完成: {}", role.getName(), responseCompleted[0]);
                        return;
                    }

                    // 跳过空token，但不跳过有效的空格或换行符
                    if (token == null || (token.isEmpty() && !token.equals(" ") && !token.equals("\n"))) {
                        log.info("角色 {} 跳过空token: '{}'", role.getName(), token);
                        return;
                    }

                    if ("[DONE]".equals(token)) {
                        log.info("角色 {} 收到[DONE]信号", role.getName());
                        // 标记响应完成
                        responseCompleted[0] = true;
                        
                        String fullResponse = responseBuilder.toString().trim();
                        log.info("角色 {} 响应生成完成，长度: {}", role.getName(), fullResponse.length());
                        
                        // 发送角色响应结束消息
                        sendMessage(emitter, "ROLE_COMPLETE", "角色响应完成", role.getId(), role.getName());
                        
                        // 保存AI消息到数据库
                        if (!fullResponse.isEmpty()) {
                            saveAiMessage(chatRoomId, role.getId(), fullResponse);
                        }
                        
                        // 完成CompletableFuture，表示该角色的流式响应真正完成
                        future.complete(null);
                        return;
                        
                    } else if ("[ERROR]".equals(token)) {
                        log.info("角色 {} 收到[ERROR]信号", role.getName());
                        responseCompleted[0] = true;
                        log.error("角色 {} 响应生成出错", role.getName());
                        sendMessage(emitter, "ROLE_ERROR", "角色响应出错", role.getId(), role.getName());
                        // 完成CompletableFuture，即使出错也要标记完成
                        future.completeExceptionally(new RuntimeException("角色响应生成出错"));
                        
                    } else {
                        // 正常的响应token
                        log.info("角色 {} 处理正常token: '{}', 进入else分支", role.getName(), token);
                        responseBuilder.append(token);
                        // 发送ROLE_MESSAGE
                        log.info("角色 {} 发送ROLE_MESSAGE: '{}'", role.getName(), token);
                        sendMessage(emitter, "ROLE_MESSAGE", token, role.getId(), role.getName());
                    }
                    
                } catch (Exception e) {
                    log.error("处理角色 {} 响应token失败", role.getName(), e);
                    if (!responseCompleted[0]) {
                        sendMessage(emitter, "ROLE_ERROR", "处理响应失败", role.getId(), role.getName());
                        future.completeExceptionally(e);
                    }
                }
            });

        } catch (Exception e) {
            log.error("生成角色 {} 流式响应失败", role.getName(), e);
            sendMessage(emitter, "ROLE_ERROR", "生成响应失败: " + e.getMessage(), role.getId(), role.getName());
            future.completeExceptionally(e);
        }
    }

    // 辅助方法：保存AI消息到数据库
    private void saveAiMessage(Long chatRoomId, Long roleId, String content) {
        try {
            chatroomMessageService.saveAiMessage(chatRoomId, roleId, content);
            log.info("AI消息已保存到聊天室 {}, 角色ID: {}", chatRoomId, roleId);
        } catch (Exception e) {
            log.error("保存AI消息失败，角色ID: {}", roleId, e);

        }
    }

    /**
     * 获取聊天室的所有角色
     */
    private List<Role> getChatRoomRoles(Long chatRoomId) {
        try {
            // 获取聊天室中用户选择的角色
            List<ChatRoom> chatRoomRoles = chatRoomService.getRolesByChatRoomId(chatRoomId);
            
            if (chatRoomRoles.isEmpty()) {
                log.info("聊天室 {} 中没有选择的角色，使用所有公开角色", chatRoomId);
                // 如果聊天室中没有选择角色，则使用所有公开角色作为备选
                List<Role> roles = roleMapper.findPublicRoles();
                log.info("获取到 {} 个公开角色作为备选", roles.size());
                return roles;
            }
            
            // 根据角色ID获取角色详情
            List<Role> roles = new ArrayList<>();
            for (ChatRoom chatRoom : chatRoomRoles) {
                if (chatRoom.getRoleId() != null) {
                    Role role = roleMapper.selectById(chatRoom.getRoleId());
                    if (role != null) {
                        roles.add(role);
                    }
                }
            }
            
            log.info("获取到聊天室 {} 中的 {} 个选择角色", chatRoomId, roles.size());
            return roles;
        } catch (Exception e) {
            log.error("获取聊天室角色失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 发送SSE消息
     */

    private synchronized void sendMessage(SseEmitter emitter, String type, String message, Long roleId, String roleName) {

        try {
            Map<String, Object> data = new HashMap<>();
            data.put("type", type);
            data.put("message", message);
            data.put("timestamp", System.currentTimeMillis());

            if (roleId != null) {
                data.put("roleId", roleId);
            }
            if (roleName != null) {
                data.put("roleName", roleName);
            }

            String jsonMessage = JSONUtil.toJsonStr(data);

            
            // 添加发送前的状态检查日志
            log.debug("准备发送SSE消息: {}, emitter状态: {}", message, getEmitterState(emitter));
            
            emitter.send(SseEmitter.event().data(jsonMessage));
            log.debug("SSE消息发送成功: {}", message);

        } catch (IllegalStateException e) {
            // SSE连接已完成，静默处理
            log.debug("SSE连接已完成，无法发送消息: {}", message);
            // 添加堆栈跟踪以找出连接完成的原因
            log.debug("SSE连接完成时的堆栈跟踪:", e);
        } catch (Exception e) {
            log.error("发送SSE消息失败", e);
            // 不要在这里调用completeWithError，避免过早关闭连接
        }
    }
    
    /**
     * 获取SSE emitter状态的辅助方法
     */
    private String getEmitterState(SseEmitter emitter) {
        try {
            // 尝试多种方式获取emitter状态
            Class<?> emitterClass = emitter.getClass();
            
            // 方法1：尝试获取complete字段
            try {
                java.lang.reflect.Field completeField = emitterClass.getDeclaredField("complete");
                completeField.setAccessible(true);
                boolean isComplete = (boolean) completeField.get(emitter);
                return isComplete ? "COMPLETED" : "ACTIVE";
            } catch (NoSuchFieldException e1) {
                // 方法2：尝试从父类获取
                try {
                    java.lang.reflect.Field completeField = emitterClass.getSuperclass().getDeclaredField("complete");
                    completeField.setAccessible(true);
                    boolean isComplete = (boolean) completeField.get(emitter);
                    return isComplete ? "COMPLETED" : "ACTIVE";
                } catch (Exception e2) {
                    // 方法3：尝试调用toString查看状态
                    String toString = emitter.toString();
                    if (toString.contains("complete")) {
                        return "COMPLETED_FROM_STRING";
                    }
                    return "ACTIVE_FROM_STRING";
                }
            }
        } catch (Exception e) {
            log.debug("获取emitter状态失败: {}", e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }


    private void handleError(SseEmitter emitter, Exception e) {
        try {
            sendMessage(emitter, "ERROR", e.getMessage(), null, null);
            emitter.complete();
        } catch (Exception sendEx) {
            log.error("发送错误消息失败", sendEx);
            emitter.completeWithError(sendEx);
        }
    }
}
