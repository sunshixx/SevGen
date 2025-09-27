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
import java.util.Map;

/**
 * AI聊天服务实现类
 */

@Service
public class AiChatServiceImpl implements IAiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatServiceImpl.class);

    private final StreamingChatLanguageModel streamingChatLanguageModel;

    /**
     * 构造函数注入，遵循依赖倒置原则
     */
    @Autowired
    public AiChatServiceImpl(StreamingChatLanguageModel streamingChatLanguageModel) {
        this.streamingChatLanguageModel = streamingChatLanguageModel;
    }




    @Override
    public String generateCharacterResponse(String roleName, String characterPrompt, String userMessage) {
        return "";
    }


    @Override
    public void generateStreamResponseDirect(String fullPrompt, StreamResponseCallback callback) {
        log.debug("开始直接使用完整prompt生成流式AI回复");

        StringBuilder aiAnswer = new StringBuilder();
        StringBuilder tokenBuffer = new StringBuilder();
        final int TOKEN_BATCH_SIZE = 200; // 每批推送字符数，可根据实际情况调整

        try {
            List<ChatMessage> messages = List.of(
                    UserMessage.from(fullPrompt)
            );
            log.info("传给llm的信息："+messages);

            streamingChatLanguageModel.generate(messages, new StreamingResponseHandler<AiMessage>() {
                @Override
                public void onNext(String token) {
                    aiAnswer.append(token);
                    tokenBuffer.append(token);
                    // 每到一定长度就推送一次，避免单次 token 过长
                    if (tokenBuffer.length() >= TOKEN_BATCH_SIZE) {
                        callback.onResponse(tokenBuffer.toString());
                        tokenBuffer.setLength(0);
                    }
                }

                @Override
                public void onError(Throwable error) {
                    log.error("直接prompt流式生成AI回复失败", error);
                    callback.onResponse("[ERROR]");
                }

                @Override
                public void onComplete(Response<AiMessage> response) {
                    // 推送剩余未发出的 token
                    if (tokenBuffer.length() > 0) {
                        callback.onResponse(tokenBuffer.toString());
                        tokenBuffer.setLength(0);
                    }
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