package com.aichat.roleplay.service.impl;

import com.aichat.roleplay.dto.RoleSelectionResult;
import com.aichat.roleplay.model.Role;
import com.aichat.roleplay.service.IRoleSelector;
import com.aichat.roleplay.service.IRoleService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于LLM的角色选择器实现
 */
@Service
public class LLMRoleSelector implements IRoleSelector {

    private static final Logger log = LoggerFactory.getLogger(LLMRoleSelector.class);

    private final IRoleService roleService;
    private final ChatLanguageModel chatModel;

    @Autowired
    public LLMRoleSelector(IRoleService roleService, ChatLanguageModel chatModel) {
        this.roleService = roleService;
        this.chatModel = chatModel;
    }

    @Override
    public RoleSelectionResult selectRoles(Long userId, String query, String context) {
        log.debug("开始选择角色: userId={}, query={}", userId, query);
        
        // 获取所有可用角色
        List<Role> availableRoles = roleService.getAllPublicRoles();
        
        // 构建提示词
        String prompt = buildPrompt(query, context, availableRoles);
        
        // 调用LLM选择角色
        String response = chatModel.generate(prompt);
        
        // 解析LLM响应
        return parseResponse(response);
    }

    private String buildPrompt(String query, String context, List<Role> availableRoles) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("作为一个角色选择专家，请根据用户的输入选择最合适的角色组合来处理请求。\n\n");
        
        // 添加用户输入信息
        prompt.append("用户输入：").append(query).append("\n");
        if (context != null && !context.isEmpty()) {
            prompt.append("对话上下文：").append(context).append("\n");
        }
        
        // 添加可用角色信息
        prompt.append("\n可用角色列表：\n");
        for (Role role : availableRoles) {
            prompt.append("- ").append(role.getName()).append("：")
                  .append(role.getDescription()).append("\n");
        }
        
        // 添加输出格式要求
        prompt.append("\n请以JSON格式返回选择结果，包含以下信息：\n");
        prompt.append("1. roles: 角色列表，每个角色包含：\n");
        prompt.append("   - roleName: 角色名称\n");
        prompt.append("   - executionOrder: 执行顺序（0表示并行执行）\n");
        prompt.append("   - customPrompt: 针对该角色的特定提示词（可选）\n");
        prompt.append("   - reason: 选择该角色的原因\n");
        prompt.append("2. isParallel: 是否并行执行（true/false）\n\n");
        prompt.append("示例输出：\n");
        prompt.append("{\n");
        prompt.append("  \"roles\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"roleName\": \"分析师\",\n");
        prompt.append("      \"executionOrder\": 1,\n");
        prompt.append("      \"customPrompt\": \"请分析以下问题...\",\n");
        prompt.append("      \"reason\": \"需要深入分析用户的问题\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"isParallel\": false\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }

    private RoleSelectionResult parseResponse(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            
            // 解析角色列表
            List<RoleSelectionResult.RoleSelection> roles = new ArrayList<>();
            JsonNode rolesNode = root.get("roles");
            if (rolesNode != null && rolesNode.isArray()) {
                for (JsonNode roleNode : rolesNode) {
                    String roleName = roleNode.get("roleName").asText();
                    int executionOrder = roleNode.get("executionOrder").asInt();
                    String customPrompt = roleNode.has("customPrompt") ? roleNode.get("customPrompt").asText() : null;
                    String reason = roleNode.get("reason").asText();
                    
                    roles.add(new RoleSelectionResult.RoleSelection(
                            roleName,
                            executionOrder,
                            customPrompt,
                            reason
                    ));
                }
            }
            
            // 解析是否并行执行
            boolean isParallel = root.has("isParallel") && root.get("isParallel").asBoolean();
            
            return new RoleSelectionResult(roles, isParallel);
            
        } catch (Exception e) {
            log.error("解析LLM响应失败", e);
            throw new RuntimeException("解析角色选择结果失败: " + e.getMessage());
        }
    }
}