package com.aichat.roleplay.service;

import com.aichat.roleplay.model.ReflectionResult;

/**
 * 相当于一个拦截模块，用于处理AI回复质量问题
 */
public interface IReflectionAgentService {
    
    /**
     * 对AI回复进行反思分析
     */
    ReflectionResult reflect(String originalQuery, String aiResponse, Long chatId, Long roleId, int currentRetryCount);
    
    /**
     * 检测AI回复中的异常情况
     */
    double detectAnomalyScore(String aiResponse, String originalQuery);
    
    /**
     * 重新生成优化的用户查询
     */
    String regenerateQuery(String originalQuery, String aiResponse, String detectedIssue, int retryCount);
    
    /**
     * 生成最终的反思总结回复
     */
    String generateFinalResponse(String originalQuery, String aiResponse);
}