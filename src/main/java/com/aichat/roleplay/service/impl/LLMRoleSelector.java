package com.aichat.roleplay.service.impl;

import com.aichat.roleplay.dto.RoleSelectionResult;
import com.aichat.roleplay.model.Role;
import com.aichat.roleplay.service.IRoleSelector;
import com.aichat.roleplay.service.IRoleService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于LLM的角色选择器实现
 */
@Service
public class LLMRoleSelector implements IRoleSelector {

    private static final Logger log = LoggerFactory.getLogger(LLMRoleSelector.class);

    private final IRoleService roleService;
    private final RoleSelectionService roleSelectionService;

    @Autowired
    public LLMRoleSelector(IRoleService roleService, ChatLanguageModel chatModel) {
        this.roleService = roleService;
        this.roleSelectionService = AiServices.builder(RoleSelectionService.class)
                .chatLanguageModel(chatModel)
                .build();
    }

    /**
     * 角色选择服务接口，使用LangChain4j的结构化输出
     */
    interface RoleSelectionService {
        RoleSelectionResult analyzeAndSelectRoles(String userQuery, String context, String availableRoles);
    }

    @Override
    public RoleSelectionResult selectRoles(Long userId, String query, String context) {
        log.debug("开始选择角色: userId={}, query={}", userId, query);
        
        // 获取所有可用角色
        List<Role> availableRoles = roleService.getAllPublicRoles();
        
        // 构建角色信息字符串
        String rolesInfo = buildRolesInfo(availableRoles);
        
        try {
            // 使用LangChain4j的结构化输出直接调用
            return roleSelectionService.analyzeAndSelectRoles(query, context, rolesInfo);
        } catch (Exception e) {
            log.error("LLM角色选择失败，使用fallback逻辑", e);
            // 如果LLM调用失败，返回默认选择
            return createFallbackSelection(availableRoles, query);
        }
    }

    private String buildRolesInfo(List<Role> roles) {
        StringBuilder sb = new StringBuilder();
        sb.append("可用角色列表：\n");
        for (Role role : roles) {
            sb.append("- ").append(role.getName()).append("：")
              .append(role.getDescription()).append("\n");
        }
        return sb.toString();
    }

    private RoleSelectionResult createFallbackSelection(List<Role> availableRoles, String query) {
        // 简单的fallback逻辑：选择第一个角色
        if (!availableRoles.isEmpty()) {
            Role firstRole = availableRoles.get(0);
            List<RoleSelectionResult.RoleSelection> selections = new ArrayList<>();
            selections.add(new RoleSelectionResult.RoleSelection(
                    firstRole.getName(),
                    1,
                    null,
                    "默认选择"
            ));
            return new RoleSelectionResult(selections, false);
        }
        return new RoleSelectionResult(new ArrayList<>(), false);
    }
}