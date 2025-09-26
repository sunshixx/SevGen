package com.aichat.roleplay.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 关键词管理工具类
 * 提供动态加载和管理异常检测关键词的功能
 */
@Slf4j
@Component
public class KeywordManager {
    
    /**
     * 从指定文件加载关键词
     */
    public static List<String> loadKeywords(String filePath) {
        List<String> keywords = new ArrayList<>();
        
        try {
            ClassPathResource resource = new ClassPathResource(filePath);
            if (!resource.exists()) {
                log.warn("关键词文件不存在: {}", filePath);
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
                
                log.info("从文件 {} 加载了 {} 个关键词", filePath, keywords.size());
            }
            
        } catch (Exception e) {
            log.error("加载关键词文件失败: {}", filePath, e);
        }
        
        return keywords;
    }
}