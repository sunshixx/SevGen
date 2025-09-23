package com.aichat.roleplay.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * LangChain4j OpenAI配置类
 * 遵循单例模式和依赖倒置原则
 */
@Configuration
public class LangChain4jConfig {

    @Value("${langchain4j.open-ai.chat-model.api-key:sk-test}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name:gpt-4}")
    private String modelName;

    @Value("${langchain4j.open-ai.chat-model.temperature:0.7}")
    private Double temperature;

    /**
     * 创建ChatLanguageModel Bean
     * 使用建造者模式配置OpenAI客户端
     *
     * @return ChatLanguageModel实例
     */
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .timeout(Duration.ofSeconds(30))
                .build();
    }
}