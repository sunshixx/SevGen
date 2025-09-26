package com.aichat.roleplay.controller;

import com.aichat.roleplay.common.ApiResponse;
import com.aichat.roleplay.context.UserContext;
import com.aichat.roleplay.model.ReflectionLog;
import com.aichat.roleplay.model.User;
import com.aichat.roleplay.service.IReflectionLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 反思日志控制器
 * 提供反思日志查询和统计功能
 */
@RestController
@RequestMapping("/api/reflection")
public class ReflectionController {
    
    private static final Logger log = LoggerFactory.getLogger(ReflectionController.class);
    
    @Autowired
    private IReflectionLogService reflectionLogService;
    
    /**
     * 获取指定聊天的反思日志
     */
    @GetMapping("/logs/chat/{chatId}")
    public ApiResponse<List<ReflectionLog>> getChatReflectionLogs(@PathVariable Long chatId) {
        try {
            User currentUser = getCurrentUser();
            log.info("查询聊天反思日志 - chatId: {}, user: {}", chatId, currentUser.getUsername());
            
            List<ReflectionLog> logs = reflectionLogService.getReflectionLogsByChatId(chatId);
            return ApiResponse.success("获取反思日志成功", logs);
            
        } catch (Exception e) {
            log.error("获取聊天反思日志失败", e);
            return ApiResponse.error("获取反思日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户的反思日志
     */
    @GetMapping("/logs/user")
    public ApiResponse<List<ReflectionLog>> getUserReflectionLogs(@RequestParam(defaultValue = "50") int limit) {
        try {
            User currentUser = getCurrentUser();
            log.info("查询用户反思日志 - userId: {}, limit: {}", currentUser.getId(), limit);
            
            List<ReflectionLog> logs = reflectionLogService.getReflectionLogsByUserId(currentUser.getId(), limit);
            return ApiResponse.success("获取用户反思日志成功", logs);
            
        } catch (Exception e) {
            log.error("获取用户反思日志失败", e);
            return ApiResponse.error("获取反思日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取重试统计信息
     */
    @GetMapping("/stats/retry")
    public ApiResponse<List<ReflectionLog>> getRetryStatistics() {
        try {
            log.info("查询重试统计信息");
            
            List<ReflectionLog> stats = reflectionLogService.getRetryStatistics();
            return ApiResponse.success("获取重试统计成功", stats);
            
        } catch (Exception e) {
            log.error("获取重试统计失败", e);
            return ApiResponse.error("获取重试统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取异常检测统计信息
     */
    @GetMapping("/stats/anomaly")
    public ApiResponse<List<ReflectionLog>> getAnomalyStatistics() {
        try {
            log.info("查询异常检测统计信息");
            
            List<ReflectionLog> stats = reflectionLogService.getAnomalyStatistics();
            return ApiResponse.success("获取异常统计成功", stats);
            
        } catch (Exception e) {
            log.error("获取异常统计失败", e);
            return ApiResponse.error("获取异常统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取时间范围内的反思日志
     */
    @GetMapping("/logs/range")
    public ApiResponse<List<ReflectionLog>> getReflectionLogsByTimeRange(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        try {
            log.info("查询时间范围内反思日志 - 开始时间: {}, 结束时间: {}", startTime, endTime);
            
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            
            List<ReflectionLog> logs = reflectionLogService.getReflectionLogsByTimeRange(start, end);
            return ApiResponse.success("获取时间范围内反思日志成功", logs);
            
        } catch (Exception e) {
            log.error("获取时间范围内反思日志失败", e);
            return ApiResponse.error("获取反思日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户
     */
    private User getCurrentUser() {
        User user = UserContext.getCurrentUser();
        if (user == null) {
            throw new RuntimeException("用户未登录");
        }
        return user;
    }
}