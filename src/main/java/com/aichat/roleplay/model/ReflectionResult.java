package com.aichat.roleplay.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 反思结果封装类
 * 用于封装反思Agent的分析结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReflectionResult {
    
    /**
     * 反思动作类型
     */
    public enum ActionType {
        SUCCESS,    // 回复正常，无需重试
        RETRY,      // 需要重试
        ERROR       // 达到最大重试次数或流程错误
    }
    
    /**
     * 反思动作
     */
    private ActionType action;
    
    /**
     * 重试次数
     */
    private Integer retryCount;
    
    /**
     * 重新生成的用户问题（当action为RETRY时使用）
     */
    private String regeneratedQuery;
    
    /**
     * 最终回复内容（当action为SUCCESS时使用）
     */
    private String finalResponse;
    
    /**
     * 错误信息（当action为ERROR时使用）
     */
    private String errorMessage;
    
    /**
     * 反思原因分析
     */
    private String reasonAnalysis;
    
    /**
     * 检测到的异常类型
     */
    private String detectedIssue;
    
    /**
     * 是否需要重试
     */
    public boolean needsRetry() {
        return action == ActionType.RETRY;
    }
    
    /**
     * 是否成功完成
     */
    public boolean isSuccess() {
        return action == ActionType.SUCCESS;
    }
    
    /**
     * 是否出现错误
     */
    public boolean isError() {
        return action == ActionType.ERROR;
    }
}