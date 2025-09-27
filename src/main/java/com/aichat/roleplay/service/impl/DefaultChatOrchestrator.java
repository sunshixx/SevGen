package com.aichat.roleplay.service.impl;

import com.aichat.roleplay.dto.*;
import com.aichat.roleplay.service.IChatOrchestrator;
import com.aichat.roleplay.service.IRoleSelector;
import com.aichat.roleplay.service.IRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 聊天编排服务实现类
 */
@Service
public class DefaultChatOrchestrator implements IChatOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(DefaultChatOrchestrator.class);

    private final IRoleSelector roleSelector;
    private final IRoleService roleService;

    @Autowired
    public DefaultChatOrchestrator(
            IRoleSelector roleSelector,
            IRoleService roleService) {
        this.roleSelector = roleSelector;
        this.roleService = roleService;
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        log.debug("开始处理聊天请求: userId={}, query={}", request.getUserId(), request.getQuery());
        
        // 1. 选择角色
        RoleSelectionResult selectionResult = roleSelector.selectRoles(
                request.getUserId(),
                request.getQuery(),
                request.getContext()
        );
        log.debug("角色选择结果: isParallel={}, rolesCount={}", 
                selectionResult.isParallel(), 
                selectionResult.getRoles().size());
        
        // 2. 生成角色响应
        List<ChatResponse.RoleResponse> roleResponses = generateRoleResponses(selectionResult, request);
        
        // 3. 聚合响应
        String finalResponse = aggregateResponses(roleResponses);
        
        return new ChatResponse(
                request.getUserId(),
                request.getQuery(),
                roleResponses,
                finalResponse
        );
    }

    private List<ChatResponse.RoleResponse> generateRoleResponses(RoleSelectionResult selectionResult, ChatRequest request) {
        if (selectionResult.isParallel()) {
            return generateParallelResponses(selectionResult, request);
        } else {
            return generateSequentialResponses(selectionResult, request);
        }
    }

    private List<ChatResponse.RoleResponse> generateParallelResponses(RoleSelectionResult selectionResult, ChatRequest request) {
        List<CompletableFuture<ChatResponse.RoleResponse>> futures = selectionResult.getRoles().stream()
                .map(role -> generateRoleResponseAsync(role, request))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()))
                .join();
    }

    private List<ChatResponse.RoleResponse> generateSequentialResponses(RoleSelectionResult selectionResult, ChatRequest request) {
        List<ChatResponse.RoleResponse> responses = new ArrayList<>();
        StringBuilder context = new StringBuilder(request.getContext() != null ? request.getContext() : "");

        selectionResult.getRoles().stream()
                .sorted(Comparator.comparingInt(RoleSelectionResult.RoleSelection::getExecutionOrder))
                .forEach(role -> {
                    ChatResponse.RoleResponse response = generateRoleResponse(role, request, context.toString());
                    responses.add(response);
                    context.append("\n").append(role.getRoleName()).append(": ").append(response.getResponse());
                });

        return responses;
    }

    @Async
    private CompletableFuture<ChatResponse.RoleResponse> generateRoleResponseAsync(
            RoleSelectionResult.RoleSelection role,
            ChatRequest request) {
        return CompletableFuture.completedFuture(
                generateRoleResponse(role, request, request.getContext())
        );
    }

    private ChatResponse.RoleResponse generateRoleResponse(
            RoleSelectionResult.RoleSelection role,
            ChatRequest request,
            String context) {
        String prompt = role.getCustomPrompt() != null ? role.getCustomPrompt() : request.getQuery();
        String response = roleService.generateResponse(role.getRoleName(), prompt, context);
        
        return new ChatResponse.RoleResponse(
                role.getRoleName(),
                response,
                role.getExecutionOrder()
        );
    }

    private String aggregateResponses(List<ChatResponse.RoleResponse> responses) {
        if (responses.isEmpty()) {
            return "";
        }
        
        if (responses.size() == 1) {
            return responses.get(0).getResponse();
        }
        
        return responses.stream()
                .sorted(Comparator.comparingInt(ChatResponse.RoleResponse::getExecutionOrder))
                .map(response -> response.getRoleName() + ": " + response.getResponse())
                .collect(Collectors.joining("\n\n"));
    }
}