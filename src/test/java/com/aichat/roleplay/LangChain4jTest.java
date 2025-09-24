package com.aichat.roleplay;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class LangChain4jTest {

    private static final Logger log = LoggerFactory.getLogger(LangChain4jTest.class);

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @Value("${langchain4j.open-ai.chat-model.base-url}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.chat-model.model-name}")
    private String modelName;

    @Test
    public void testConfigurationLoads() {
        log.info("正在测试LangChain4j配置加载...");
        log.info("Base URL: {}", baseUrl);
        log.info("Model Name: {}", modelName);
        
        assertThat(chatLanguageModel).isNotNull();
        log.info("ChatLanguageModel配置加载成功!");
    }

    @Test
    public void testAiModelConnection() {
        log.info("开始测试AI模型连接...");
        log.info("使用端点: {}", baseUrl);
        log.info("使用模型: {}", modelName);
        
        String question = "你好，请简单回复一句话。";
        log.info("发送问题: {}", question);
        
        try {
            String response = chatLanguageModel.generate(question);
            log.info("收到回复: {}", response);
            
            assertThat(response).isNotNull().isNotEmpty();
            log.info("AI模型连接测试通过!");
            
        } catch (Exception e) {
            log.error("AI模型连接测试失败", e);
            throw e;
        }
    }
}