package com.aichat.roleplay.service.impl;

import com.aichat.roleplay.config.ReflectionConfig;
import com.aichat.roleplay.model.ReflectionResult;
import com.aichat.roleplay.service.IReflectionAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

/**
 * 反思Agent服务实现类 - 重构版本
 * 负责分析AI回复质量，判断是否需要重试
 */
@Service
public class ReflectionAgentServiceImpl implements IReflectionAgentService {
    
    private static final Logger log = LoggerFactory.getLogger(ReflectionAgentServiceImpl.class);
    
    @Autowired
    private ReflectionConfig reflectionConfig;

    // 检测结果缓存，避免重复计算
    private static class DetectionResult {
        double score;
        String issue;
        
        DetectionResult(double score, String issue) {
            this.score = score;
            this.issue = issue;
        }
    }

    @Override
    public ReflectionResult reflect(String originalQuery, String aiResponse, Long chatId, Long roleId, int currentRetryCount) {
        log.info("开始反思分析 - chatId: {}, roleId: {}, retryCount: {}", chatId, roleId, currentRetryCount);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 检查是否启用反思功能
            if (!reflectionConfig.isEnabled()) {
                log.debug("反思功能已禁用，直接返回成功");
                return ReflectionResult.builder()
                        .action(ReflectionResult.ActionType.SUCCESS)
                        .finalResponse(aiResponse)
                        .retryCount(currentRetryCount)
                        .reasonAnalysis("反思功能已禁用")
                        .build();
            }
            
            // 检查是否超过最大重试次数
            if (currentRetryCount >= reflectionConfig.getMaxRetryCount()) {
                log.warn("达到最大重试次数: {}", currentRetryCount);
                return ReflectionResult.builder()
                        .action(ReflectionResult.ActionType.ERROR)
                        .retryCount(currentRetryCount)
                        .errorMessage("达到最大重试次数")
                        .reasonAnalysis("重试次数超过限制: " + reflectionConfig.getMaxRetryCount())
                        .build();
            }
            
            // 统一异常检测 - 一次性完成所有检测并获取详细结果
            AnomalyAnalysisResult analysisResult = performComprehensiveAnalysis(aiResponse, originalQuery);
            
            log.debug("异常检测完成 - 异常分数: {}, 检测问题: {}", analysisResult.totalScore, analysisResult.detectedIssue);
            log.info("问题分析详情 - 检测到的具体问题: [{}]", analysisResult.detectedIssue);
            
            // 判断是否需要重试
            if (analysisResult.totalScore > reflectionConfig.getAnomalyDetection().getSevereAnomalyThreshold()) {
                // 严重异常，直接返回错误
                return ReflectionResult.builder()
                        .action(ReflectionResult.ActionType.ERROR)
                        .retryCount(currentRetryCount)
                        .errorMessage("检测到严重异常，无法通过重试解决")
                        .detectedIssue(analysisResult.detectedIssue)
                        .reasonAnalysis(String.format("严重异常(分数: %.2f): %s", analysisResult.totalScore, analysisResult.detectedIssue))
                        .build();
                        
            } else if (analysisResult.totalScore > reflectionConfig.getAnomalyDetection().getAnomalyThreshold() && !analysisResult.detectedIssue.isEmpty()) {
                // 需要重试
                String regeneratedQuery = regenerateQuery(originalQuery, aiResponse, analysisResult.detectedIssue, currentRetryCount);
                
                return ReflectionResult.builder()
                        .action(ReflectionResult.ActionType.RETRY)
                        .retryCount(currentRetryCount + 1)
                        .regeneratedQuery("RETRY" + (currentRetryCount + 1) + " " + regeneratedQuery)
                        .detectedIssue(analysisResult.detectedIssue)
                        .reasonAnalysis(String.format("检测到异常(分数: %.2f): %s", analysisResult.totalScore, analysisResult.detectedIssue))
                        .build();
            } else {
                // 回复正常，生成最终回复
                String finalResponse = generateFinalResponse(originalQuery, aiResponse);
                
                return ReflectionResult.builder()
                        .action(ReflectionResult.ActionType.SUCCESS)
                        .finalResponse(finalResponse)
                        .retryCount(currentRetryCount)
                        .reasonAnalysis(String.format("回复正常(异常分数: %.2f)", analysisResult.totalScore))
                        .build();
            }
            
        } catch (Exception e) {
            log.error("反思分析过程中发生异常", e);
            return ReflectionResult.builder()
                    .action(ReflectionResult.ActionType.ERROR)
                    .retryCount(currentRetryCount)
                    .errorMessage("反思分析异常: " + e.getMessage())
                    .reasonAnalysis("系统内部错误")
                    .build();
        } finally {
            long processingTime = System.currentTimeMillis() - startTime;
            log.debug("反思分析完成，耗时: {}ms", processingTime);
        }
    }

    @Override
    public double detectAnomalyScore(String aiResponse, String originalQuery) {
        return performComprehensiveAnalysis(aiResponse, originalQuery).totalScore;
    }

    /**
     * 统一的综合异常分析方法 - 消除重复逻辑的核心方法
     */
    private AnomalyAnalysisResult performComprehensiveAnalysis(String aiResponse, String originalQuery) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return new AnomalyAnalysisResult(1.0, "回复为空");
        }
        
        // 预处理文本，避免重复转换
        String responseLower = aiResponse.toLowerCase();
        String queryLower = originalQuery.toLowerCase();
        
        // 统一检测所有维度，避免重复计算
        Map<String, DetectionResult> detectionResults = new HashMap<>();
        
        // 1. 角色一致性检查 (权重40% - 最重要)
        detectionResults.put("role", detectRoleInconsistencyUnified(responseLower));
        
        // 2. 关键词异常检测 (权重25%)
        detectionResults.put("keyword", detectKeywordAnomalyUnified(responseLower));
        
        // 3. 内容相关性检查 (权重15%)
        detectionResults.put("relevance", detectContentRelevanceUnified(responseLower, queryLower));
        
        // 4. 语言一致性检查 (权重10%)
        detectionResults.put("language", detectLanguageInconsistencyUnified(aiResponse, originalQuery));
        
        // 5. 情感一致性检查 (权重5%)
        detectionResults.put("emotion", detectEmotionConsistencyUnified(responseLower, queryLower));
        
        // 6. 长度检查 (权重5%)
        detectionResults.put("length", detectLengthAnomalyUnified(aiResponse));
        
        // 计算加权总分
        double totalScore = 0.0;
        totalScore += detectionResults.get("role").score * 0.40;
        totalScore += detectionResults.get("keyword").score * 0.25;
        totalScore += detectionResults.get("relevance").score * 0.15;
        totalScore += detectionResults.get("language").score * 0.10;
        totalScore += detectionResults.get("emotion").score * 0.05;
        totalScore += detectionResults.get("length").score * 0.05;
        
        // 使用sigmoid函数平滑化分数
        double finalScore = Math.min(1.0, 1.0 / (1.0 + Math.exp(-6.0 * (totalScore - 0.5))));
        
        // 收集所有检测到的问题
        StringBuilder issues = new StringBuilder();
        for (Map.Entry<String, DetectionResult> entry : detectionResults.entrySet()) {
            if (entry.getValue().score > 0.2 && !entry.getValue().issue.isEmpty()) {
                if (issues.length() > 0) issues.append("; ");
                issues.append(entry.getValue().issue);
            }
        }
        
        // 统一日志输出
        if (log.isInfoEnabled()) {
            log.info("反思检测详情 - 角色(40%): {:.3f}, 关键词(25%): {:.3f}, 相关性(15%): {:.3f}, 语言(10%): {:.3f}, 情感(5%): {:.3f}, 长度(5%): {:.3f}",
                    detectionResults.get("role").score * 0.40,
                    detectionResults.get("keyword").score * 0.25,
                    detectionResults.get("relevance").score * 0.15,
                    detectionResults.get("language").score * 0.10,
                    detectionResults.get("emotion").score * 0.05,
                    detectionResults.get("length").score * 0.05);
            log.info("总分: {:.3f} -> 最终异常分数: {:.3f} (阈值: {})", 
                    totalScore, finalScore, reflectionConfig.getAnomalyDetection().getAnomalyThreshold());
        }
        
        return new AnomalyAnalysisResult(finalScore, issues.toString());
    }

    /**
     * 统一的角色一致性检测
     */
    private DetectionResult detectRoleInconsistencyUnified(String responseLower) {
        // 1. 严重AI身份暴露
        String[] aiIdentityTerms = {"我是ai", "我是人工智能", "我是机器人", "我是助手", "我是程序", 
                                   "语言模型", "gpt", "chatgpt", "大语言模型", "llm", 
                                   "as an ai", "i am an ai", "artificial intelligence"};
        
        for (String term : aiIdentityTerms) {
            if (responseLower.contains(term)) {
                log.info("检测到严重角色暴露: {}", term);
                return new DetectionResult(1.0, "严重角色暴露: " + term);
            }
        }
        
        // 2. 角色扮演暴露
        String[] rolePlayTerms = {"角色扮演", "我在扮演", "这是角色扮演", "我需要扮演", "role-playing", "i'm playing"};
        for (String term : rolePlayTerms) {
            if (responseLower.contains(term)) {
                return new DetectionResult(0.95, "角色扮演暴露");
            }
        }
        
        // 3. 技术术语检测
        String[] techTerms = {"程序", "算法", "代码", "数据库", "系统", "模型", "训练", "API"};
        int techCount = (int) Arrays.stream(techTerms).mapToLong(term -> 
            responseLower.split(term, -1).length - 1).sum();
        
        if (techCount >= 2) {
            return new DetectionResult(0.8, "多个技术术语暴露");
        } else if (techCount == 1) {
            return new DetectionResult(0.5, "技术术语暴露");
        }
        
        // 4. 过度谦让检测
        String[] politeTerms = {"很抱歉", "非常抱歉", "深感抱歉", "我不能", "我无法", "我不会"};
        int politeCount = (int) Arrays.stream(politeTerms).mapToLong(term -> 
            responseLower.split(term, -1).length - 1).sum();
        
        if (politeCount >= 3) {
            return new DetectionResult(0.7, "过度道歉");
        } else if (politeCount >= 2) {
            return new DetectionResult(0.4, "适度谦让");
        }
        
        return new DetectionResult(0.0, "");
    }

    /**
     * 统一的关键词异常检测
     */
    private DetectionResult detectKeywordAnomalyUnified(String responseLower) {
        List<String> anomalousKeywords = reflectionConfig.getAnomalousKeywords();
        StringBuilder matchedKeywords = new StringBuilder();
        int matchCount = 0;
        
        for (String keyword : anomalousKeywords) {
            if (responseLower.contains(keyword.toLowerCase())) {
                matchCount++;
                if (matchedKeywords.length() > 0) matchedKeywords.append(", ");
                matchedKeywords.append(keyword);
            }
        }
        
        if (matchCount > 0) {
            double score = Math.min(1.0, matchCount * 0.3);
            String issue = "异常关键词: " + matchedKeywords.toString();
            log.debug("检测到{}个异常关键词: {}", matchCount, matchedKeywords);
            return new DetectionResult(score, issue);
        }
        
        return new DetectionResult(0.0, "");
    }

    /**
     * 统一的内容相关性检测
     */
    private DetectionResult detectContentRelevanceUnified(String responseLower, String queryLower) {
        // 拒绝回答检测
        String[] refusalTerms = {"我不知道", "无法回答", "这个问题超出了我的能力", 
                                "i don't know", "i can't answer", "beyond my capabilities"};
        
        for (String term : refusalTerms) {
            if (responseLower.contains(term)) {
                return new DetectionResult(0.7, "拒绝回答或表示不知道");
            }
        }
        
        // 简单重复检测
        if (responseLower.contains(queryLower) && responseLower.length() < queryLower.length() + 20) {
            return new DetectionResult(0.6, "简单重复问题");
        }
        
        // 通用回复检测
        String[] genericTerms = {"很高兴为您服务", "有什么可以帮助您", "请问还有什么问题", "感谢您的提问", "希望能帮到您"};
        for (String term : genericTerms) {
            if (responseLower.contains(term) && responseLower.replace(term, "").trim().length() < 10) {
                return new DetectionResult(0.5, "通用回复，缺乏针对性");
            }
        }
        
        return new DetectionResult(0.0, "");
    }

    /**
     * 统一的语言一致性检测
     */
    private DetectionResult detectLanguageInconsistencyUnified(String aiResponse, String originalQuery) {
        boolean queryHasChinese = containsChinese(originalQuery);
        boolean responseHasChinese = containsChinese(aiResponse);
        
        if (queryHasChinese && !responseHasChinese && aiResponse.length() > 50) {
            return new DetectionResult(0.7, "语言不一致：中文问题英文回答");
        }
        if (!queryHasChinese && responseHasChinese && !originalQuery.toLowerCase().contains("chinese")) {
            return new DetectionResult(0.7, "语言不一致：英文问题中文回答");
        }
        
        return new DetectionResult(0.0, "");
    }

    /**
     * 统一的情感一致性检测
     */
    private DetectionResult detectEmotionConsistencyUnified(String responseLower, String queryLower) {
        boolean queryPositive = containsPositiveWords(queryLower);
        boolean queryNegative = containsNegativeWords(queryLower);
        boolean responseNegative = containsNegativeWords(responseLower);
        
        if (queryPositive && responseNegative) {
            return new DetectionResult(0.6, "情感不一致：积极问题消极回答");
        }
        
        if (!queryNegative && (responseLower.contains("抱歉") || responseLower.contains("对不起") || 
            responseLower.contains("很遗憾") || responseLower.contains("unfortunately"))) {
            return new DetectionResult(0.5, "过度道歉或消极回应");
        }
        
        return new DetectionResult(0.0, "");
    }

    /**
     * 统一的长度异常检测
     */
    private DetectionResult detectLengthAnomalyUnified(String aiResponse) {
        int length = aiResponse.length();
        int minLength = reflectionConfig.getAnomalyDetection().getMinResponseLength();
        int maxLength = reflectionConfig.getAnomalyDetection().getMaxResponseLength();
        
        if (length < minLength) {
            return new DetectionResult(0.8, "回复过短(" + length + "字符)");
        }
        if (length > maxLength) {
            return new DetectionResult(0.6, "回复过长(" + length + "字符)");
        }
        
        return new DetectionResult(0.0, "");
    }

    /**
     * 检测积极词汇
     */
    private boolean containsPositiveWords(String text) {
        String[] positiveWords = {"好", "棒", "喜欢", "想要", "需要", "请", "谢谢", "great", "good", "like", "want", "please"};
        return Arrays.stream(positiveWords).anyMatch(text::contains);
    }
    
    /**
     * 检测消极词汇
     */
    private boolean containsNegativeWords(String text) {
        String[] negativeWords = {"不", "没有", "不能", "无法", "错误", "失败", "no", "can't", "cannot", "error", "fail"};
        return Arrays.stream(negativeWords).anyMatch(text::contains);
    }
    
    /**
     * 检测文本是否包含中文
     */
    private boolean containsChinese(String text) {
        if (text == null) return false;
        return text.matches(".*[\\u4e00-\\u9fa5].*");
    }
    
    @Override
    public String regenerateQuery(String originalQuery, String aiResponse, String detectedIssue, int retryCount) {
        log.debug("重新生成查询 - 原查询: {}, 检测问题: {}, 重试次数: {}", 
                 originalQuery.substring(0, Math.min(50, originalQuery.length())), detectedIssue, retryCount);
        
        String cleanedQuery = cleanOriginalQuery(originalQuery);
        log.info("重试查询清理完成: {}", cleanedQuery.substring(0, Math.min(100, cleanedQuery.length())));
        return cleanedQuery;
    }
    
    /**
     * 清理原始查询中的系统提示词
     */
    private String cleanOriginalQuery(String query) {
        String cleaned = query;
        
        // 移除所有可能的系统提示词
        String[] patterns = {
            "\\s*\\(请用更清晰的方式回答\\)\\s*",
            "\\s*\\(请用更.*?的方式回答\\)\\s*",
            "\\s*\\(请提供更.*?的回答\\)\\s*",
            "\\s*\\(请.*?回答\\)\\s*",
            "\\s*请用更清晰的方式回答\\s*",
            "\\s*请提供更准确的回答\\s*",
            "\\s*请再试一次\\s*"
        };
        
        for (String pattern : patterns) {
            cleaned = cleaned.replaceAll(pattern, "");
        }
        
        // 清理多余的空格
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        
        return cleaned.isEmpty() ? query : cleaned;
    }
    
    @Override
    public String generateFinalResponse(String originalQuery, String aiResponse) {
        log.debug("生成最终回复");
        return aiResponse.trim();
    }

    /**
     * 异常分析结果封装类
     */
    private static class AnomalyAnalysisResult {
        final double totalScore;
        final String detectedIssue;
        
        AnomalyAnalysisResult(double totalScore, String detectedIssue) {
            this.totalScore = totalScore;
            this.detectedIssue = detectedIssue;
        }
    }
}