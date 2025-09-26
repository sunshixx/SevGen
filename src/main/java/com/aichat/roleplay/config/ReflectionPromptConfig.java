package com.aichat.roleplay.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 反思Prompt模板管理器
 * 负责加载和管理各种prompt模板
 */
@Component
public class ReflectionPromptConfig {
    
    private static final Logger log = LoggerFactory.getLogger(ReflectionPromptConfig.class);
    
    private final Map<String, String> promptTemplates = new HashMap<>();
    
    @PostConstruct
    public void loadPrompts() {
        try {
            ClassPathResource resource = new ClassPathResource("prompts/reflection-prompts.properties");
            Properties properties = new Properties();
            
            try (InputStream inputStream = resource.getInputStream()) {
                properties.load(inputStream);
                
                for (String key : properties.stringPropertyNames()) {
                    promptTemplates.put(key, properties.getProperty(key));
                }
                
                log.info("成功加载 {} 个反思Prompt模板", promptTemplates.size());
            }
            
        } catch (IOException e) {
            log.error("加载反思Prompt模板失败", e);
            // 加载默认模板作为备用
            loadDefaultPrompts();
        }
    }
    
    /**
     * 获取基础反思模板
     */
    public String getBasicReflectionPrompt(int retryCount, int maxRetryCount) {
        String template = promptTemplates.getOrDefault("reflection.basic.prompt", getDefaultBasicPrompt());
        return template.replace("{retryCount}", String.valueOf(retryCount))
                      .replace("{maxRetryCount}", String.valueOf(maxRetryCount));
    }
    
    /**
     * 获取查询重新生成模板
     */
    public String getRegenerationPrompt(String originalQuery, String aiResponse, String detectedIssue) {
        String template = promptTemplates.getOrDefault("reflection.regeneration.prompt", getDefaultRegenerationPrompt());
        return template.replace("{originalQuery}", originalQuery)
                      .replace("{aiResponse}", aiResponse)
                      .replace("{detectedIssue}", detectedIssue);
    }
    
    /**
     * 获取异常分析模板
     */
    public String getAnalysisPrompt(String originalQuery, String aiResponse) {
        String template = promptTemplates.getOrDefault("reflection.analysis.prompt", getDefaultAnalysisPrompt());
        return template.replace("{originalQuery}", originalQuery)
                      .replace("{aiResponse}", aiResponse);
    }
    
    /**
     * 获取最终优化模板
     */
    public String getFinalOptimizationPrompt(String originalResponse, String originalQuery) {
        String template = promptTemplates.getOrDefault("reflection.final.prompt", getDefaultFinalPrompt());
        return template.replace("{originalResponse}", originalResponse)
                      .replace("{originalQuery}", originalQuery);
    }
    
    /**
     * 获取错误处理模板
     */
    public String getErrorHandlingPrompt(String originalQuery, String lastResponse, String mainIssues) {
        String template = promptTemplates.getOrDefault("reflection.error.prompt", getDefaultErrorPrompt());
        return template.replace("{originalQuery}", originalQuery)
                      .replace("{lastResponse}", lastResponse)
                      .replace("{mainIssues}", mainIssues);
    }
    
    /**
     * 获取角色验证模板
     */
    public String getRoleValidationPrompt(String roleName, String rolePrompt, String aiResponse, String originalQuery) {
        String template = promptTemplates.getOrDefault("reflection.role.validation.prompt", getDefaultRoleValidationPrompt());
        return template.replace("{roleName}", roleName)
                      .replace("{rolePrompt}", rolePrompt)
                      .replace("{aiResponse}", aiResponse)
                      .replace("{originalQuery}", originalQuery);
    }
    
    /**
     * 加载默认模板作为备用
     */
    private void loadDefaultPrompts() {
        promptTemplates.put("reflection.basic.prompt", getDefaultBasicPrompt());
        promptTemplates.put("reflection.regeneration.prompt", getDefaultRegenerationPrompt());
        promptTemplates.put("reflection.analysis.prompt", getDefaultAnalysisPrompt());
        promptTemplates.put("reflection.final.prompt", getDefaultFinalPrompt());
        promptTemplates.put("reflection.error.prompt", getDefaultErrorPrompt());
        promptTemplates.put("reflection.role.validation.prompt", getDefaultRoleValidationPrompt());
        
        log.info("加载默认反思Prompt模板作为备用");
    }
    
    private String getDefaultBasicPrompt() {
        return "你现在负责整理多智能体的输出，生成最终回复。" +
               "输入包含各 Agent 的输出和用户问题。" +
               "如果发现流程异常，有两种情况：" +
               "1. 可通过重新生成问题并执行流程解决，请在开头输出RETRY加重试次数{retryCount}便于程序做判断。" +
               "2. 工作流或重试次数超过{maxRetryCount}次，请在开头输出ERROR，并分析原因。";
    }
    
    private String getDefaultRegenerationPrompt() {
        return "用户原始问题：{originalQuery}\\n" +
               "AI回复内容：{aiResponse}\\n" +
               "检测到的问题：{detectedIssue}\\n" +
               "请重新生成一个更清晰的用户问题，保持原意不变。";
    }
    
    private String getDefaultAnalysisPrompt() {
        return "请分析以下AI回复是否存在问题：\\n" +
               "用户问题：{originalQuery}\\n" +
               "AI回复：{aiResponse}\\n" +
               "请给出0-1的异常分数。";
    }
    
    private String getDefaultFinalPrompt() {
        return "请对以下AI回复进行最终优化：\\n" +
               "原始回复：{originalResponse}\\n" +
               "用户问题：{originalQuery}\\n" +
               "请优化语言表达，使其更加自然、友好和专业。";
    }
    
    private String getDefaultErrorPrompt() {
        return "检测到多次重试后仍存在问题：\\n" +
               "原始问题：{originalQuery}\\n" +
               "最后一次回复：{lastResponse}\\n" +
               "主要问题：{mainIssues}\\n" +
               "请生成一个礼貌的道歉回复。";
    }
    
    private String getDefaultRoleValidationPrompt() {
        return "请验证以下回复是否符合{roleName}的角色设定：\\n" +
               "角色设定：{rolePrompt}\\n" +
               "AI回复：{aiResponse}\\n" +
               "用户问题：{originalQuery}\\n" +
               "给出符合度分数(0-1)和改进建议。";
    }
}