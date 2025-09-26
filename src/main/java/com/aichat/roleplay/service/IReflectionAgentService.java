package com.aichat.roleplay.service;

import com.aichat.roleplay.model.ReflectionResult;

/**
 * 相当于一个拦截模块，用于处理AI回复质量问题
 */
public interface IReflectionAgentService {
    
    /**
     * 对AI回复进行反思分析
     *
     * @param originalQuery 原始用户查询
     * @param aiResponse AI回复内容
     * @param chatId 聊天会话ID
     * @param roleId 角色ID
     * @param currentRetryCount 当前重试次数
     * @return 反思结果
     */
    ReflectionResult reflect(String originalQuery, String aiResponse, Long chatId, Long roleId, int currentRetryCount);
    
    /**
     * 检测AI回复中的异常情况
     *
     * @param aiResponse AI回复内容
     * @param originalQuery 原始用户查询
     * @return 异常分数（0-1，越高越异常）
     */
    double detectAnomalyScore(String aiResponse, String originalQuery);
    
    /**
     * 重新生成优化的用户查询
     *
     * @param originalQuery 原始查询
     * @param aiResponse 有问题的AI回复
     * @param detectedIssue 检测到的问题
     * @param retryCount 重试次数
     * @return 优化后的查询
     */
    String regenerateQuery(String originalQuery, String aiResponse, String detectedIssue, int retryCount);
    
    /**
     * 生成最终的反思总结回复
     *
     * @param originalQuery 原始查询
     * @param aiResponse AI回复
     * @return 最终回复
     */
    String generateFinalResponse(String originalQuery, String aiResponse);
}