package com.aichat.roleplay.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class LangChain4jConfig {

    @Value("${langchain4j.open-ai.chat-model.base-url:https://api.openai.com/v1}")
    private String chatBaseUrl;

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String chatApiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name:gpt-3.5-turbo}")
    private String chatModelName;

    @Value("${langchain4j.open-ai.chat-model.temperature:0.7}")
    private Double temperature;

    @Value("${langchain4j.open-ai.speech-to-text-model.model-name:whisper-1}")
    private String asrModelName;

    @Value("${langchain4j.open-ai.text-to-speech-model.model-name:tts}")
    private String ttsModelName;

    @Value("${langchain4j.open-ai.text-to-speech-model.voice:alloy}")
    private String ttsVoice;


    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(chatBaseUrl)
                .apiKey(chatApiKey)
                .modelName(chatModelName)
                .temperature(temperature)
                .timeout(Duration.ofSeconds(60))
                .build();


    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    @Bean("speechToTextConfig")
    public VoiceModelConfig speechToTextConfig() {
        return new VoiceModelConfig(chatBaseUrl, chatApiKey, asrModelName);
    }


    @Bean("textToSpeechConfig")
    public VoiceModelConfig textToSpeechConfig() {
        return new VoiceModelConfig(chatBaseUrl, chatApiKey, ttsModelName, ttsVoice);
    }


    public static class VoiceModelConfig {
        private final String baseUrl;
        private final String apiKey;
        private final String modelName;
        private final String voice;

        public VoiceModelConfig(String baseUrl, String apiKey, String modelName) {
            this(baseUrl, apiKey, modelName, null);
        }

        public VoiceModelConfig(String baseUrl, String apiKey, String modelName, String voice) {
            this.baseUrl = baseUrl;
            this.apiKey = apiKey;
            this.modelName = modelName;
            this.voice = voice;
        }

        public String getBaseUrl() { return baseUrl; }
        public String getApiKey() { return apiKey; }
        public String getModelName() { return modelName; }
        public String getVoice() { return voice; }
    }
}