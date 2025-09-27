package com.aichat.roleplay.service;

import com.aichat.roleplay.model.ReflectionLog;
import com.aichat.roleplay.model.ReflectionResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 反思日志服务接口，后续可以介入普罗米修斯和Grafana之类的可视化
 */
public interface IReflectionLogService {
    
    /**
     * 保存反思日志
     */
    void saveReflectionLog(Long chatId, Long roleId, Long userId, String originalQuery, String aiResponse, ReflectionResult result, long processingTime);
    
    /**
     * 根据聊天ID查询反思日志
     */
    List<ReflectionLog> getReflectionLogsByChatId(Long chatId);
    
    /**
     * 根据用户ID查询反思日志
     */
    List<ReflectionLog> getReflectionLogsByUserId(Long userId, int limit);
    
    /**
     * 查询时间范围内的反思日志
     */
    List<ReflectionLog> getReflectionLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取重试统计信息
     */
    List<ReflectionLog> getRetryStatistics();
    
    /**
     * 获取异常检测统计信息
     */
    List<ReflectionLog> getAnomalyStatistics();
}