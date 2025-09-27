package com.aichat.roleplay.service.impl;

import com.aichat.roleplay.config.ReflectionConfig;
import com.aichat.roleplay.model.ReflectionResult;
import com.aichat.roleplay.service.IReflectionAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 反思Agent服务实现类
 * 负责分析AI回复质量，判断是否需要重试
 */
@Service
public class ReflectionAgentServiceImpl implements IReflectionAgentService {
    
    private static final Logger log = LoggerFactory.getLogger(ReflectionAgentServiceImpl.class);
    
    @Autowired
    private ReflectionConfig reflectionConfig;
    
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
            
            // 异常检测
            double anomalyScore = detectAnomalyScore(aiResponse, originalQuery);
            String detectedIssue = analyzeDetectedIssue(aiResponse, anomalyScore);
            
            log.debug("异常检测完成 - 异常分数: {}, 检测问题: {}", anomalyScore, detectedIssue);
            log.info("问题分析详情 - 检测到的具体问题: [{}]", detectedIssue);
            
            // 判断是否需要重试
            if (anomalyScore > reflectionConfig.getAnomalyDetection().getSevereAnomalyThreshold()) {
                // 严重异常，直接返回错误
                return ReflectionResult.builder()
                        .action(ReflectionResult.ActionType.ERROR)
                        .retryCount(currentRetryCount)
                        .errorMessage("检测到严重异常，无法通过重试解决")
                        .detectedIssue(detectedIssue)
                        .reasonAnalysis(String.format("严重异常(分数: %.2f): %s", anomalyScore, detectedIssue))
                        .build();
                        
            } else if (anomalyScore > reflectionConfig.getAnomalyDetection().getAnomalyThreshold() && !detectedIssue.isEmpty()) {
                // 需要重试
                String regeneratedQuery = regenerateQuery(originalQuery, aiResponse, detectedIssue, currentRetryCount);
                
                return ReflectionResult.builder()
                        .action(ReflectionResult.ActionType.RETRY)
                        .retryCount(currentRetryCount + 1)
                        .regeneratedQuery("RETRY" + (currentRetryCount + 1) + " " + regeneratedQuery)
                        .detectedIssue(detectedIssue)
                        .reasonAnalysis(String.format("检测到异常(分数: %.2f): %s", anomalyScore, detectedIssue))
                        .build();
            } else {
                // 回复正常，生成最终回复
                String finalResponse = generateFinalResponse(originalQuery, aiResponse);
                
                return ReflectionResult.builder()
                        .action(ReflectionResult.ActionType.SUCCESS)
                        .finalResponse(finalResponse)
                        .retryCount(currentRetryCount)
                        .reasonAnalysis(String.format("回复正常(异常分数: %.2f)", anomalyScore))
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
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return 1.0; // 空回复，最高异常分数
        }
        
        double totalScore = 0.0;
        
        // 1. 角色一致性检查 (权重40% - 最重要) ⭐
        double roleScore = detectRoleInconsistency(aiResponse);
        totalScore += roleScore * 0.40;
        if (roleScore > 0) {
            log.info("角色一致性检测异常，分数: {}, 贡献: {}", 
                String.format("%.2f", roleScore), String.format("%.2f", roleScore * 0.40));
        }
        
        // 2. 关键词异常检测 (权重25% - 身份暴露等严重问题)
        double keywordScore = detectKeywordAnomaly(aiResponse);
        totalScore += keywordScore * 0.25;
        if (keywordScore > 0) {
            log.info("关键词异常检测，分数: {}, 贡献: {}", 
                String.format("%.2f", keywordScore), String.format("%.2f", keywordScore * 0.25));
        }
        
        // 3. 内容相关性检查 (权重15% - 是否切题)
        double relevanceScore = detectContentRelevance(aiResponse, originalQuery);
        totalScore += relevanceScore * 0.15;
        
        // 4. 语言一致性检查 (权重10% - 语言风格)
        double languageScore = detectLanguageInconsistency(aiResponse, originalQuery);
        totalScore += languageScore * 0.10;
        
        // 5. 情感一致性检查 (权重5% - 情感色彩)
        double emotionScore = detectEmotionConsistency(aiResponse, originalQuery);
        totalScore += emotionScore * 0.05;
        
        // 6. 长度检查 (权重5% - 基础检查)
        double lengthScore = detectLengthAnomaly(aiResponse);
        totalScore += lengthScore * 0.05;
        
        // 计算最终异常分数
        double finalScore = Math.min(1.0, 1.0 / (1.0 + Math.exp(-6.0 * (totalScore - 0.5))));
        
        // 详细日志输出 - 重点关注角色一致性
        log.info("反思检测详情 - 角色一致性(40%): {}, 关键词(25%): {}, " +
                "内容相关(15%): {}, 语言(10%): {}, 情感(5%): {}, 长度(5%): {}", 
                String.format("%.3f", roleScore * 0.40), 
                String.format("%.3f", keywordScore * 0.25), 
                String.format("%.3f", relevanceScore * 0.15), 
                String.format("%.3f", languageScore * 0.10), 
                String.format("%.3f", emotionScore * 0.05), 
                String.format("%.3f", lengthScore * 0.05));
        log.info("总分: {} -> 最终异常分数: {} (阈值: {})", 
                String.format("%.3f", totalScore), 
                String.format("%.3f", finalScore), 
                reflectionConfig.getAnomalyDetection().getAnomalyThreshold());
        
        // 使用sigmoid函数平滑化分数，避免过于极端
        return finalScore;
    }
    
    /**
     * 检测关键词异常
     */
    private double detectKeywordAnomaly(String aiResponse) {
        String response = aiResponse.toLowerCase();
        List<String> anomalousKeywords = reflectionConfig.getAnomalousKeywords();
        
        int matchCount = 0;
        for (String keyword : anomalousKeywords) {
            if (response.contains(keyword.toLowerCase())) {
                matchCount++;
                log.debug("检测到异常关键词: '{}' 在回复中: '{}'", keyword, response.substring(0, Math.min(50, response.length())));
            }
        }
        
        if (matchCount > 0) {
            log.info("关键词异常匹配: {}个关键词", matchCount);
        }
        
        // 异常关键词匹配越多，异常分数越高
        return Math.min(1.0, matchCount * 0.3);
    }
    
    /**
     * 检测长度异常
     */
    private double detectLengthAnomaly(String aiResponse) {
        int length = aiResponse.length();
        int minLength = reflectionConfig.getAnomalyDetection().getMinResponseLength();
        int maxLength = reflectionConfig.getAnomalyDetection().getMaxResponseLength();
        
        if (length < minLength) {
            return 0.8; // 太短
        }
        if (length > maxLength) {
            return 0.6; // 太长
        }
        return 0.0; // 长度正常
    }
    
    /**
     * 检测语言不一致性
     */
    private double detectLanguageInconsistency(String aiResponse, String originalQuery) {
        // 简单的中英文检测
        boolean queryHasChinese = containsChinese(originalQuery);
        boolean responseHasChinese = containsChinese(aiResponse);
        
        // 如果用户用中文提问，但AI回复全是英文，或反之，则可能存在语言不一致
        if (queryHasChinese && !responseHasChinese && aiResponse.length() > 50) {
            return 0.7;
        }
        if (!queryHasChinese && responseHasChinese && !originalQuery.toLowerCase().contains("chinese")) {
            return 0.7;
        }
        
        return 0.0;
    }
    
    /**
     * 检测角色不一致性 - 增强版本，作为最重要的检测维度
     */
    private double detectRoleInconsistency(String aiResponse) {
        String response = aiResponse.toLowerCase();
        
        // 1. 【严重】明确暴露AI身份 - 立即触发重试
        if (response.contains("我是ai") || response.contains("我是人工智能") || 
            response.contains("我是机器人") || response.contains("我是助手") ||
            response.contains("我是程序") || response.contains("语言模型") ||
            response.contains("gpt") || response.contains("chatgpt") || 
            response.contains("大语言模型") || response.contains("llm") ||
            response.contains("as an ai") || response.contains("i am an ai") ||
            response.contains("artificial intelligence")) {
            log.info("检测到严重角色暴露: AI身份相关词汇");
            return 1.0; // 最高异常分数
        }
        
        // 2. 【严重】明确说明在进行角色扮演
        if (response.contains("角色扮演") || response.contains("我在扮演") || 
            response.contains("这是角色扮演") || response.contains("我需要扮演") ||
            response.contains("role-playing") || response.contains("i'm playing")) {
            return 0.95;
        }
        
        // 3. 【中等】技术术语暴露 - 破坏沉浸感
        String[] techTerms = {"程序", "算法", "代码", "数据库", "系统", "模型", "训练", "API"};
        int techTermCount = 0;
        for (String term : techTerms) {
            if (response.contains(term)) {
                techTermCount++;
            }
        }
        if (techTermCount >= 2) {
            return 0.8; // 多个技术术语
        } else if (techTermCount == 1) {
            return 0.5; // 单个技术术语
        }
        
        // 4. 【中等】过度谦让，不符合角色设定
        String[] overPoliteTerms = {"很抱歉", "非常抱歉", "深感抱歉", "我不能", "我无法", "我不会"};
        int politeCount = 0;
        for (String term : overPoliteTerms) {
            if (response.contains(term)) {
                politeCount++;
            }
        }
        if (politeCount >= 3) {
            return 0.7; // 过度道歉
        } else if (politeCount >= 2) {
            return 0.4; // 适度谦让
        }
        
        // 5. 【轻微】元认知表达 - 提及自己的思考过程
        if (response.contains("我认为") || response.contains("我觉得") || 
            response.contains("据我了解") || response.contains("从我的角度")) {
            return 0.3;
        }
        
        // 6. 【检查】回复是否过于机械化
        if (response.startsWith("根据") && (response.contains("信息") || response.contains("数据"))) {
            return 0.4;
        }
        
        return 0.0; // 角色表现正常
    }
    
    /**
     * 检测内容相关性 - 通用版本
     */
    private double detectContentRelevance(String aiResponse, String originalQuery) {
        String response = aiResponse.toLowerCase();
        String query = originalQuery.toLowerCase();
        
        // 1. 检测是否完全偏离主题或拒绝回答
        if (response.contains("我不知道") || response.contains("无法回答") || 
            response.contains("这个问题超出了我的能力") || response.contains("i don't know") ||
            response.contains("i can't answer") || response.contains("beyond my capabilities")) {
            return 0.7;
        }
        
        // 2. 检测是否只是简单重复问题而没有实质回答
        if (response.contains(query) && response.length() < query.length() + 20) {
            return 0.6;
        }
        
        // 3. 检测是否回复了完全不相关的通用内容
        String[] genericResponses = {
            "很高兴为您服务", "有什么可以帮助您", "请问还有什么问题",
            "感谢您的提问", "希望能帮到您"
        };
        
        for (String generic : genericResponses) {
            if (response.contains(generic) && response.replace(generic, "").trim().length() < 10) {
                return 0.5;
            }
        }
        
        // 4. 检测回复长度与问题复杂度的匹配性
        if (query.length() > 20 && response.length() < 10) {
            return 0.4; // 复杂问题得到过于简单的回答
        }
        
        return 0.0;
    }
    
    /**
     * 检测情感一致性 - 新增
     */
    private double detectEmotionConsistency(String aiResponse, String originalQuery) {
        String response = aiResponse.toLowerCase();
        String query = originalQuery.toLowerCase();
        
        // 检测情感词汇
        boolean queryPositive = containsPositiveWords(query);
        boolean queryNegative = containsNegativeWords(query);
        boolean responseNegative = containsNegativeWords(response);
        
        // 用户积极询问，但AI回复消极
        if (queryPositive && responseNegative) {
            return 0.6;
        }
        
        // 用户正常询问，但AI过度道歉或消极
        if (!queryNegative && (response.contains("抱歉") || response.contains("对不起") || 
            response.contains("很遗憾") || response.contains("unfortunately"))) {
            return 0.5;
        }
        
        return 0.0;
    }
    
    /**
     * 检测积极词汇
     */
    private boolean containsPositiveWords(String text) {
        String[] positiveWords = {"好", "棒", "喜欢", "想要", "需要", "请", "谢谢", "great", "good", "like", "want", "please"};
        return java.util.Arrays.stream(positiveWords).anyMatch(text::contains);
    }
    
    /**
     * 检测消极词汇
     */
    private boolean containsNegativeWords(String text) {
        String[] negativeWords = {"不", "没有", "不能", "无法", "错误", "失败", "no", "can't", "cannot", "error", "fail"};
        return java.util.Arrays.stream(negativeWords).anyMatch(text::contains);
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
        
        // 不再使用AI重新生成查询，直接返回清理后的原始查询
        // 这样避免了累积系统提示词的问题，同时保持查询的原始意图
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
        cleaned = cleaned.replaceAll("\\s*\\(请用更清晰的方式回答\\)\\s*", "");
        cleaned = cleaned.replaceAll("\\s*\\(请用更.*?的方式回答\\)\\s*", "");
        cleaned = cleaned.replaceAll("\\s*\\(请提供更.*?的回答\\)\\s*", "");
        cleaned = cleaned.replaceAll("\\s*\\(请.*?回答\\)\\s*", "");
        cleaned = cleaned.replaceAll("\\s*请用更清晰的方式回答\\s*", "");
        cleaned = cleaned.replaceAll("\\s*请提供更准确的回答\\s*", "");
        cleaned = cleaned.replaceAll("\\s*请再试一次\\s*", "");
        
        // 清理多余的空格
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        
        return cleaned.isEmpty() ? query : cleaned;
    }
    
    @Override
    public String generateFinalResponse(String originalQuery, String aiResponse) {
        log.debug("生成最终响应 - 原查询: {}, AI响应: {}", 
                 originalQuery.substring(0, Math.min(50, originalQuery.length())),
                 aiResponse.substring(0, Math.min(50, aiResponse.length())));
        
        // 直接返回AI响应，不再进行额外处理
        // 这样避免了可能的循环优化问题
        return aiResponse;
    }
    
    /**
     * 分析检测到的具体问题
     */
    private String analyzeDetectedIssue(String aiResponse, double anomalyScore) {
        // 只要超过基础阈值就开始分析问题，不设置0.4的限制
        if (anomalyScore <= reflectionConfig.getAnomalyDetection().getAnomalyThreshold()) {
            return "";
        }
        
        StringBuilder issues = new StringBuilder();
        
        // 1. 检查角色一致性问题（最重要）
        double roleScore = detectRoleInconsistency(aiResponse);
        if (roleScore > 0.5) {
            if (issues.length() > 0) issues.append("; ");
            issues.append("严重角色暴露或角色不一致");
        } else if (roleScore > 0.2) {
            if (issues.length() > 0) issues.append("; ");
            issues.append("轻微角色不一致");
        }
        
        // 2. 检查异常关键词
        for (String keyword : reflectionConfig.getAnomalousKeywords()) {
            if (aiResponse.toLowerCase().contains(keyword.toLowerCase())) {
                if (issues.length() > 0) issues.append("; ");
                issues.append("包含异常关键词: ").append(keyword);
            }
        }
        
        // 3. 检查长度异常
        int length = aiResponse.length();
        if (length < reflectionConfig.getAnomalyDetection().getMinResponseLength()) {
            if (issues.length() > 0) issues.append("; ");
            issues.append("回复过短(").append(length).append("字符)");
        }
        
        if (length > reflectionConfig.getAnomalyDetection().getMaxResponseLength()) {
            if (issues.length() > 0) issues.append("; ");
            issues.append("回复过长(").append(length).append("字符)");
        }
        
        // 4. 检查重复内容
        if (hasRepeatedContent(aiResponse)) {
            if (issues.length() > 0) issues.append("; ");
            issues.append("内容重复或循环");
        }
        
        // 4. 检查格式问题
        if (hasPoorFormatting(aiResponse)) {
            if (issues.length() > 0) issues.append("; ");
            issues.append("格式混乱或结构不清");
        }
        
        // 5. 检查逻辑一致性
        if (hasLogicalInconsistency(aiResponse)) {
            if (issues.length() > 0) issues.append("; ");
            issues.append("逻辑不一致或自相矛盾");
        }
        
        // 6. 检查专业性
        if (lacksProfessionalism(aiResponse)) {
            if (issues.length() > 0) issues.append("; ");
            issues.append("缺乏专业性或过于随意");
        }
        
        // 7. 检查完整性
        if (isIncompleteResponse(aiResponse)) {
            if (issues.length() > 0) issues.append("; ");
            issues.append("回复不完整或突然中断");
        }
        
        // 8. 检查安全性
        if (containsUnsafeContent(aiResponse)) {
            if (issues.length() > 0) issues.append("; ");
            issues.append("包含不当或敏感内容");
        }
        
        return issues.toString();
    }
    
    /**
     * 检测重复内容
     */
    private boolean hasRepeatedContent(String text) {
        String[] sentences = text.split("[。！？.!?]");
        if (sentences.length < 3) return false;
        
        for (int i = 0; i < sentences.length - 1; i++) {
            for (int j = i + 1; j < sentences.length; j++) {
                if (sentences[i].trim().equals(sentences[j].trim()) && sentences[i].trim().length() > 10) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 检测格式问题
     */
    private boolean hasPoorFormatting(String text) {
        // 检测是否有过多的空格、换行或特殊字符
        long spaceCount = text.chars().filter(c -> c == ' ').count();
        long totalChars = text.length();
        
        if (totalChars > 0 && spaceCount / (double) totalChars > 0.3) {
            return true;
        }
        
        // 检测是否有不匹配的括号或引号
        int openBrackets = text.length() - text.replace("(", "").length();
        int closeBrackets = text.length() - text.replace(")", "").length();
        
        return Math.abs(openBrackets - closeBrackets) > 2;
    }
    
    /**
     * 检测逻辑不一致性
     */
    private boolean hasLogicalInconsistency(String text) {
        String lower = text.toLowerCase();
        
        // 检测矛盾表述
        if ((lower.contains("是") && lower.contains("不是")) ||
            (lower.contains("可以") && lower.contains("不可以")) ||
            (lower.contains("有") && lower.contains("没有"))) {
            return true;
        }
        
        // 检测时态混乱
        if (lower.contains("昨天") && lower.contains("明天") && text.length() < 100) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 检测专业性缺失
     */
    private boolean lacksProfessionalism(String text) {
        String lower = text.toLowerCase();
        
        // 检测过于口语化或不当表达
        String[] unprofessionalWords = {
            "哈哈", "呵呵", "嘻嘻", "哎呀", "我去", "靠", "牛逼", "屌", "lol", "lmao"
        };
        
        for (String word : unprofessionalWords) {
            if (lower.contains(word)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 检测回复完整性
     */
    private boolean isIncompleteResponse(String text) {
        String trimmed = text.trim();
        
        // 检测是否以不完整的方式结束
        if (trimmed.endsWith("...") || trimmed.endsWith("等等") || 
            trimmed.endsWith("and so on") || trimmed.endsWith("etc")) {
            return true;
        }
        
        // 检测是否在句子中间突然结束
        if (!trimmed.endsWith("。") && !trimmed.endsWith("！") && 
            !trimmed.endsWith("？") && !trimmed.endsWith(".") && 
            !trimmed.endsWith("!") && !trimmed.endsWith("?") && 
            trimmed.length() > 20) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 检测不安全内容
     */
    private boolean containsUnsafeContent(String text) {
        String lower = text.toLowerCase();
        
        // 检测可能的敏感内容
        String[] sensitiveWords = {
            "密码", "账号", "银行卡", "身份证", "password", "account", "credit card"
        };
        
        for (String word : sensitiveWords) {
            if (lower.contains(word)) {
                return true;
            }
        }
        
        return false;
    }
}