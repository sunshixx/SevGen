package com.aichat.roleplay.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 反思Agent配置类
 * 配置反思机制的各项参数
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "reflection")
public class ReflectionConfig {
    
    /**
     * 是否启用反思机制
     */
    private boolean enabled = true;
    
    /**
     * 最大重试次数
     */
    private int maxRetryCount = 3;
    
    /**
     * 反思超时时间（毫秒）
     */
    private long reflectionTimeoutMs = 30000;
    
    /**
     * 异常检测关键词列表
     * 用于检测AI回复中的异常情况
     * 启动时从文本文件中加载
     */
    private List<String> anomalousKeywords = new ArrayList<>();
    
    /**
     * 积极关键词列表
     * 用于检测回复是否符合预期，默认为空以保持通用性
     * 可根据具体业务场景在配置文件中自定义
     */
    private List<String> positiveKeywords = List.of();
    
    /**
     * 关键词文件路径配置
     */
    private KeywordFiles keywordFiles = new KeywordFiles();
    
    /**
     * 异常检测规则配置
     */
    private AnomalyDetection anomalyDetection = new AnomalyDetection();
    
    @Data
    public static class KeywordFiles {
        /**
         * 身份暴露类关键词文件路径
         */
        private String identityExposure = "anomaly-keywords/identity-exposure.txt";
        
        /**
         * 角色破防类关键词文件路径
         */
        private String roleBreaking = "anomaly-keywords/role-breaking.txt";
        
        /**
         * 过度推脱类关键词文件路径
         */
        private String overDeflection = "anomaly-keywords/over-deflection.txt";
        
        /**
         * 技术暴露类关键词文件路径
         */
        private String technicalExposure = "anomaly-keywords/technical-exposure.txt";
    }
    
    /**
     * 应用启动后加载关键词文件
     */
    @PostConstruct
    public void loadKeywordsFromFiles() {
        log.info("开始加载异常检测关键词文件...");
        
        List<String> allKeywords = new ArrayList<>();
        
        // 加载各类关键词文件
        allKeywords.addAll(loadKeywordsFromFile(keywordFiles.identityExposure));
        allKeywords.addAll(loadKeywordsFromFile(keywordFiles.roleBreaking));
        allKeywords.addAll(loadKeywordsFromFile(keywordFiles.overDeflection));
        allKeywords.addAll(loadKeywordsFromFile(keywordFiles.technicalExposure));
        
        this.anomalousKeywords = allKeywords;
        
        System.out.println("异常检测关键词加载完成！总数: " + allKeywords.size());
        
        // 显示部分关键词样本
        System.out.println("关键词样本:");
        allKeywords.stream().limit(3).forEach(keyword -> 
            System.out.println("   • " + keyword));
    }
    
    /**
     * 从指定文件加载关键词
     */
    private List<String> loadKeywordsFromFile(String filePath) {
        List<String> keywords = new ArrayList<String>();
        
        try {
            ClassPathResource resource = new ClassPathResource(filePath);
            if (!resource.exists()) {
                System.out.println("关键词文件不存在: " + filePath);
                return keywords;
            }
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    
                    // 跳过空行和注释行
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        keywords.add(line);
                    }
                }
                
                System.out.println("从文件 " + filePath + " 加载了 " + keywords.size() + " 个关键词");
            }
            
        } catch (Exception e) {
            System.out.println("加载关键词文件失败: " + filePath + ", 错误: " + e.getMessage());
        }
        
        return keywords;
    }
    
    @Data
    public static class AnomalyDetection {
        /**
         * 最小回复长度（字符数）
         */
        private int minResponseLength = 5;
        
        /**
         * 最大回复长度（字符数）
         */
        private int maxResponseLength = 2000;
        
        /**
         * 是否检查语言一致性
         */
        private boolean checkLanguageConsistency = true;
        
        /**
         * 是否检查角色一致性
         */
        private boolean checkRoleConsistency = true;
        
        /**
         * 异常分数阈值 - 超过此值触发重试
         */
        private double anomalyThreshold = 0.4;
        
        /**
         * 严重异常阈值 - 超过此值直接标记为错误
         */
        private double severeAnomalyThreshold = 0.8;
    }
}