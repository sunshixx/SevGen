package com.aichat.roleplay.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 角色选择结果DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleSelectionResult {
    
    /**
     * 选择的角色列表
     */
    private List<RoleSelection> roles;
    
    /**
     * 是否并行执行
     */
    private boolean isParallel;
    
    /**
     * 角色选择
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleSelection {
        /**
         * 角色名称
         */
        private String roleName;
        
        /**
         * 执行顺序（0表示并行执行）
         */
        private int executionOrder;
        
        /**
         * 自定义提示词
         */
        private String customPrompt;
        
        /**
         * 选择原因
         */
        private String reason;
    }
}