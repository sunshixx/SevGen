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
    public void generateStreamResponseDirect(String fullPrompt, StreamResponseCallback callback) {
        log.debug("开始直接使用完整prompt生成流式AI回复");

        StringBuilder aiAnswer = new StringBuilder();

        try {
            List<ChatMessage> messages = List.of(
                    UserMessage.from(fullPrompt)
            );
            log.info("传给llm的信息："+messages);

            streamingChatLanguageModel.generate(messages, new StreamingResponseHandler<AiMessage>() {
                @Override
                public void onNext(String token) {
                    log.info("AI服务收到token: '{}', 长度: {}, 类型: {}", 
                            token, token != null ? token.length() : 0, 
                            token != null ? token.getClass().getSimpleName() : "null");
                    aiAnswer.append(token);
                    // 实时推送每个token，确保前端能立即接收到ROLE_MESSAGE
                    log.info("AI服务准备推送token到callback: '{}'", token);
                    try {
                        callback.onResponse(token);
                        log.info("AI服务成功推送token到callback: '{}'", token);
                    } catch (Exception e) {
                        log.error("AI服务推送token到callback失败: '{}'", token, e);
                    }
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
                    log.info("完整AI回复内容: '{}'", finalAnswer);

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