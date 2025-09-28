package com.aichat.roleplay.dto;

import java.util.List;

/**
 * 角色选择结果DTO
 * 用于LLM选择适合的角色后返回结果
 */
public class RoleSelectionResult {
    
    /**
     * 选中的角色ID列表
     */
    private List<Long> selectedRoleIds;
    
    /**
     * 选择原因说明
     */
    private String reason;
    
    /**
     * 置信度分数 (0-1)
     */
    private Double confidence;
    
    // 构造函数
    public RoleSelectionResult() {}
    
    public RoleSelectionResult(List<Long> selectedRoleIds, String reason, Double confidence) {
        this.selectedRoleIds = selectedRoleIds;
        this.reason = reason;
        this.confidence = confidence;
    }
    
    // Getters and Setters
    public List<Long> getSelectedRoleIds() {
        return selectedRoleIds;
    }
    
    public void setSelectedRoleIds(List<Long> selectedRoleIds) {
        this.selectedRoleIds = selectedRoleIds;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public Double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
    
    @Override
    public String toString() {
        return "RoleSelectionResult{" +
                "selectedRoleIds=" + selectedRoleIds +
                ", reason='" + reason + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}