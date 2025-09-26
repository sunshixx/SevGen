package com.aichat.roleplay.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 反思日志记录模型
 * 记录反思Agent的处理过程和结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("reflection_logs")
public class ReflectionLog {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 聊天会话ID
     */
    private Long chatId;
    
    /**
     * 角色ID
     */
    private Long roleId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 原始用户输入
     */
    private String originalQuery;
    
    /**
     * AI系统指令/回复
     */
    private String aiResponse;
    
    /**
     * 反思动作类型: SUCCESS, RETRY, ERROR
     */
    private String actionType;
    
    /**
     * 重试次数
     */
    private Integer retryCount;
    
    /**
     * 重新生成的查询
     */
    private String regeneratedQuery;
    
    /**
     * 最终输出
     */
    private String finalOutput;
    
    /**
     * 检测到的问题类型
     */
    private String detectedIssue;
    
    /**
     * 反思原因分析
     */
    private String reasonAnalysis;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 处理耗时（毫秒）
     */
    private Long processingTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}