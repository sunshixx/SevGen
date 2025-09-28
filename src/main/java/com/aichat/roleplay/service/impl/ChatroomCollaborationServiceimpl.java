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

    @Value("${chatroom.collaboration.top-k-roles:3}")
    private int topKRoles;

    @Value("${chatroom.collaboration.max-concurrent-roles:5}")
    private int maxConcurrentRoles;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    public SseEmitter handleCollaborativeMessage(Long chatRoomId, String userMessage, String context) {
        log.info("处理协作消息 - chatRoomId: {}, userMessage: {}", chatRoomId, userMessage);

        SseEmitter emitter = new SseEmitter(60000L);

        try {
            // 异步处理协作消息
            CompletableFuture.runAsync(() -> {
                try {
                    processCollaborativeMessage(chatRoomId, userMessage, context, emitter);
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
    private void processCollaborativeMessage(Long chatRoomId, String userMessage, String context, SseEmitter emitter) {
        try {
            // 0. 保存用户消息
            Long userId = UserContext.getCurrentUserId();
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
            
            // 构建聊天历史上下文
            String chatHistory = chatroomMessageService.buildChatHistory(chatRoomId, 10);
            String fullContext = context + (chatHistory.isEmpty() ? "" : "\n聊天历史:\n" + chatHistory);
            
            // 直接使用selectTopKRoles方法进行智能角色选择
            var selectionResult = roleSelector.selectTopKRoles(userMessage, availableRoles, actualTopK, fullContext);
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

            // 4. 并发调用每个角色的流式响应
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (Role role : selectedRoles) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        generateRoleStreamResponse(role, userMessage, chatHistory, chatRoomId, emitter);
                    } catch (Exception e) {
                        log.error("角色 {} 流式响应失败", role.getName(), e);
                        sendMessage(emitter, "ERROR", "角色响应失败: " + e.getMessage(),
                                role.getId(), role.getName());
                    }
                }, executorService);

                futures.add(future);
            }

            // 5. 等待所有角色响应完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> {
                        try {
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
    private void generateRoleStreamResponse(Role role, String userMessage, String chatHistory, Long chatRoomId, SseEmitter emitter) {
        try {
            log.info("开始生成角色 {} 的流式响应", role.getName());

            // 发送角色开始响应消息
            sendMessage(emitter, "ROLE_START", "角色开始响应", role.getId(), role.getName());

            StringBuilder responseBuilder = new StringBuilder();

            // 使用现有的generateRoleResponse方法，传入角色特定的聊天历史
            String roleChatHistory = chatroomMessageService.buildRoleChatHistory(chatRoomId, role.getId(), 10);
            String response = sseService.generateRoleResponse(role.getCharacterPrompt(), userMessage, roleChatHistory);

            // 模拟流式输出（将完整响应分块发送）
            String[] words = response.split("(?<=\\s)|(?=\\s)");
            for (String word : words) {
                responseBuilder.append(word);
                sendMessage(emitter, "ROLE_MESSAGE", word, role.getId(), role.getName());

                // 添加小延迟模拟真实流式效果
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            // 保存AI消息到数据库
            String fullResponse = responseBuilder.toString().trim();
            if (!fullResponse.isEmpty()) {
                try {
                    chatroomMessageService.saveAiMessage(chatRoomId, role.getId(), fullResponse);
                    log.info("AI消息已保存到聊天室 {}, 角色: {}", chatRoomId, role.getName());
                } catch (Exception e) {
                    log.error("保存AI消息失败，角色: {}", role.getName(), e);
                    // 不中断流程
                }
            }

            // 发送角色完成消息
            sendMessage(emitter, "ROLE_COMPLETE", "角色响应完成", role.getId(), role.getName());

            log.info("角色 {} 流式响应完成，响应长度: {}", role.getName(), responseBuilder.length());

        } catch (Exception e) {
            log.error("生成角色 {} 流式响应失败", role.getName(), e);
            sendMessage(emitter, "ROLE_ERROR", "角色响应失败: " + e.getMessage(),
                    role.getId(), role.getName());
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
    private void sendMessage(SseEmitter emitter, String type, String message, Long roleId, String roleName) {
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
            emitter.send(SseEmitter.event().data(jsonMessage));

        } catch (Exception e) {
            log.error("发送SSE消息失败", e);
            emitter.completeWithError(e);
        }
    }

    /**
     * 处理错误
     */
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
