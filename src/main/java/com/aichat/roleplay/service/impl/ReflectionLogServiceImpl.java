package com.aichat.roleplay.service.impl;

import com.aichat.roleplay.mapper.ReflectionMapper;
import com.aichat.roleplay.model.ReflectionLog;
import com.aichat.roleplay.model.ReflectionResult;
import com.aichat.roleplay.service.IReflectionLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 反思日志服务实现类
 */
@Service
public class ReflectionLogServiceImpl implements IReflectionLogService {
    
    private static final Logger log = LoggerFactory.getLogger(ReflectionLogServiceImpl.class);
    
    @Autowired
    private ReflectionMapper reflectionMapper;
    
    @Override
    public void saveReflectionLog(Long chatId, Long roleId, Long userId, String originalQuery, String aiResponse, ReflectionResult result, long processingTime) {
        try {
            ReflectionLog reflectionLog = ReflectionLog.builder()
                    .chatId(chatId)
                    .roleId(roleId)
                    .userId(userId)
                    .originalQuery(originalQuery)
                    .aiResponse(aiResponse)
                    .actionType(result.getAction().name())
                    .retryCount(result.getRetryCount())
                    .regeneratedQuery(result.getRegeneratedQuery())
                    .finalOutput(result.getFinalResponse())
                    .detectedIssue(result.getDetectedIssue())
                    .reasonAnalysis(result.getReasonAnalysis())
                    .errorMessage(result.getErrorMessage())
                    .processingTime(processingTime)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            
            reflectionMapper.insert(reflectionLog);
            log.debug("反思日志保存成功 - ID: {}", reflectionLog.getId());
            
        } catch (Exception e) {
            log.error("保存反思日志失败", e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    @Override
    public List<ReflectionLog> getReflectionLogsByChatId(Long chatId) {
        try {
            return reflectionMapper.findByChatId(chatId);
        } catch (Exception e) {
            log.error("查询聊天反思日志失败 - chatId: {}", chatId, e);
            return List.of();
        }
    }
    
    @Override
    public List<ReflectionLog> getReflectionLogsByUserId(Long userId, int limit) {
        try {
            return reflectionMapper.findByUserId(userId, limit);
        } catch (Exception e) {
            log.error("查询用户反思日志失败 - userId: {}", userId, e);
            return List.of();
        }
    }
    
    @Override
    public List<ReflectionLog> getReflectionLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            return reflectionMapper.findByTimeRange(startTime, endTime);
        } catch (Exception e) {
            log.error("查询时间范围内反思日志失败", e);
            return List.of();
        }
    }
    
    @Override
    public List<ReflectionLog> getRetryStatistics() {
        try {
            return reflectionMapper.getRetryStatistics();
        } catch (Exception e) {
            log.error("查询重试统计失败", e);
            return List.of();
        }
    }
    
    @Override
    public List<ReflectionLog> getAnomalyStatistics() {
        try {
            return reflectionMapper.getAnomalyStatistics();
        } catch (Exception e) {
            log.error("查询异常统计失败", e);
            return List.of();
        }
    }
}