package com.aichat.roleplay.service.impl;

import com.aichat.roleplay.dto.RoleSelectionResult;
import com.aichat.roleplay.model.Role;
import com.aichat.roleplay.service.IAiChatService;
import com.aichat.roleplay.service.IRoleSelector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色选择器实现类
 * 使用LLM智能选择最适合的角色参与聊天室对话
 */
@Slf4j
@Service
public class RoleSelectorImpl implements IRoleSelector {

    @Autowired
    private IAiChatService aiChatService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public RoleSelectionResult selectTopKRoles(String userMessage, List<Role> availableRoles, int topK, String context) {
        log.info("开始使用LLM选择角色，用户消息: {}, 可用角色数: {}, 选择数量: {}", userMessage, availableRoles.size(), topK);
        
        if (availableRoles == null || availableRoles.isEmpty()) {
            log.warn("没有可用角色");
            return new RoleSelectionResult(new ArrayList<>(), "没有可用角色", 0.0);
        }
        
        // 如果角色数量小于等于topK，直接返回所有角色
        if (availableRoles.size() <= topK) {
            log.info("可用角色数量({})小于等于请求数量({})，返回所有角色", availableRoles.size(), topK);
            List<Long> allRoleIds = availableRoles.stream()
                    .map(Role::getId)
                    .collect(Collectors.toList());
            return new RoleSelectionResult(allRoleIds, "角色数量不足，返回所有可用角色", 1.0);
        }
        
        try {
            // 使用LLM进行智能角色选择
            String prompt = buildRoleSelectionPrompt(userMessage, availableRoles, topK, context);
            String llmResponse = aiChatService.generateCharacterResponse("", "", prompt);
            
            // 解析LLM响应
            RoleSelectionResult result = parseLLMResponse(llmResponse, availableRoles);
            
            log.info("LLM角色选择完成，选中角色ID: {}, 原因: {}, 置信度: {}", 
                    result.getSelectedRoleIds(), result.getReason(), result.getConfidence());
            
            return result;
            
        } catch (Exception e) {
            log.error("LLM角色选择失败，回退到简单选择策略", e);
            // 回退策略：返回前topK个角色
            List<Long> fallbackRoleIds = availableRoles.stream()
                    .limit(topK)
                    .map(Role::getId)
                    .collect(Collectors.toList());
            return new RoleSelectionResult(fallbackRoleIds, "LLM选择失败，使用回退策略", 0.5);
        }
    }
    
    /**
     * 构建角色选择的LLM提示词
     */
    private String buildRoleSelectionPrompt(String userMessage, List<Role> availableRoles, int topK, String context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("你是一个智能角色选择助手。根据用户的消息内容，从给定的角色列表中选择最适合参与对话的")
              .append(topK).append("个角色。\n\n");
        
        prompt.append("用户消息：").append(userMessage).append("\n\n");
        
        if (context != null && !context.trim().isEmpty()) {
            prompt.append("上下文信息：").append(context).append("\n\n");
        }
        
        prompt.append("可选角色列表：\n");
        for (int i = 0; i < availableRoles.size(); i++) {
            Role role = availableRoles.get(i);
            prompt.append(i + 1).append(". ID: ").append(role.getId())
                  .append(", 名称: ").append(role.getName())
                  .append(", 描述: ").append(role.getDescription() != null ? role.getDescription() : "无描述")
                  .append("\n");
        }
        
        prompt.append("\n请根据用户消息的内容和语境，选择最适合参与对话的").append(topK).append("个角色。");
        prompt.append("请以JSON格式返回结果，包含以下字段：\n");
        prompt.append("{\n");
        prompt.append("  \"selectedRoleIds\": [角色ID列表],\n");
        prompt.append("  \"reason\": \"选择原因\",\n");
        prompt.append("  \"confidence\": 置信度(0.0-1.0)\n");
        prompt.append("}\n\n");
        prompt.append("只返回JSON，不要包含其他文字。");
        
        return prompt.toString();
    }
    
    /**
     * 解析LLM响应
     */
    private RoleSelectionResult parseLLMResponse(String llmResponse, List<Role> availableRoles) {
        try {
            // 清理响应文本，提取JSON部分
            String jsonStr = extractJsonFromResponse(llmResponse);
            
            JsonNode jsonNode = objectMapper.readTree(jsonStr);
            
            // 解析选中的角色ID
            List<Long> selectedRoleIds = new ArrayList<>();
            JsonNode roleIdsNode = jsonNode.get("selectedRoleIds");
            if (roleIdsNode != null && roleIdsNode.isArray()) {
                for (JsonNode idNode : roleIdsNode) {
                    selectedRoleIds.add(idNode.asLong());
                }
            }
            
            // 验证角色ID的有效性
            List<Long> validRoleIds = availableRoles.stream()
                    .map(Role::getId)
                    .collect(Collectors.toList());
            selectedRoleIds = selectedRoleIds.stream()
                    .filter(validRoleIds::contains)
                    .collect(Collectors.toList());
            
            String reason = jsonNode.has("reason") ? jsonNode.get("reason").asText() : "LLM智能选择";
            double confidence = jsonNode.has("confidence") ? jsonNode.get("confidence").asDouble() : 0.8;
            
            return new RoleSelectionResult(selectedRoleIds, reason, confidence);
            
        } catch (JsonProcessingException e) {
            log.error("解析LLM响应JSON失败: {}", llmResponse, e);
            throw new RuntimeException("解析LLM响应失败", e);
        }
    }
    
    /**
     * 从响应中提取JSON字符串
     */
    private String extractJsonFromResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            throw new RuntimeException("LLM响应为空");
        }
        
        // 查找JSON开始和结束位置
        int startIndex = response.indexOf('{');
        int endIndex = response.lastIndexOf('}');
        
        if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
            throw new RuntimeException("LLM响应中未找到有效的JSON格式");
        }
        
        return response.substring(startIndex, endIndex + 1);
    }
}