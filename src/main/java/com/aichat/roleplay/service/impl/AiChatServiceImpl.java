package com.aichat.roleplay.service.impl;

import com.aichat.roleplay.service.IAiChatService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI聊天服务实现类
 * 使用LangChain4j集成OpenAI
 * 遵循SOLID原则中的单一职责原则和依赖倒置原则
 * 采用策略模式处理不同类型的AI回复生成
 */

@Service
public class AiChatServiceImpl implements IAiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatServiceImpl.class);

    private final StreamingChatLanguageModel streamingChatLanguageModel;

    /**
     * 构造函数注入，遵循依赖倒置原则
     *
     * @param streamingChatLanguageModel 聊天语言模型
     */
    @Autowired
    public AiChatServiceImpl(StreamingChatLanguageModel streamingChatLanguageModel) {
        this.streamingChatLanguageModel = streamingChatLanguageModel;
    }


    @Override
    public String generateRoleResponse(String rolePrompt, String userMessage, String chatHistory) {
        return "";
    }

    @Override
    public String generateCharacterResponse(String roleName, String characterPrompt, String userMessage) {
        return "";
    }

    @Override
    public void generateStreamResponse(String rolePrompt, String userMessage, StreamResponseCallback callback) {
        log.debug("开始流式生成AI回复");

        // 这里应由外部构建完整 prompt 并传入，或通过 RolePromptEngineering 构建
        callback.onResponse("[ERROR] 未实现：请使用 RolePromptEngineering 构建 prompt 并调用 generateStreamResponseDirect");
    }
    
    @Override
    public void generateStreamResponseDirect(String fullPrompt, StreamResponseCallback callback) {
        log.debug("开始直接使用完整prompt生成流式AI回复");

        StringBuilder aiAnswer = new StringBuilder();

        try {
            List<ChatMessage> messages = List.of(
                    UserMessage.from(fullPrompt)
            );

            streamingChatLanguageModel.generate(messages, new StreamingResponseHandler<AiMessage>() {
                @Override
                public void onNext(String token) {
                    aiAnswer.append(token);
                    callback.onResponse(token);
                }

                @Override
                public void onError(Throwable error) {
                    log.error("直接prompt流式生成AI回复失败", error);
                    callback.onResponse("[ERROR]");
                }

                @Override
                public void onComplete(Response<AiMessage> response) {
                    String finalAnswer = aiAnswer.toString();
                    log.debug("直接prompt流式回复完成，长度: {}", finalAnswer.length());

                    if (response == null || response.content() == null) {
                        log.warn("onComplete 收到空的 response");
                    }

                    callback.onResponse("[DONE]");
                }
            });

        } catch (Exception e) {
            log.error("调用直接prompt流式API失败", e);
            callback.onResponse("[ERROR]");
        }
    }

}