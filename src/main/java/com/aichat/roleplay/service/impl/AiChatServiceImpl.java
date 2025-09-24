package com.aichat.roleplay.service.impl;

import com.aichat.roleplay.service.IAiChatService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
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

    /**
     * 构建角色提示词
     * 使用模板方法模式
     *
     * @param rolePrompt  角色提示
     * @param userMessage 用户消息
     * @param chatHistory 聊天历史
     * @return 完整提示词
     */
    private String buildRolePrompt(String rolePrompt, String userMessage, String chatHistory) {
        StringBuilder promptBuilder = new StringBuilder();

        // 添加角色设定
        if (rolePrompt != null && !rolePrompt.isEmpty()) {
            promptBuilder.append("你是一个AI角色扮演助手。请严格按照以下角色设定进行回复：\n");
            promptBuilder.append(rolePrompt).append("\n\n");
        }

        // 添加聊天历史
        if (chatHistory != null && !chatHistory.isEmpty()) {
            promptBuilder.append("聊天历史：\n");
            promptBuilder.append(chatHistory).append("\n\n");
        }

        // 添加用户消息
        promptBuilder.append("用户说：").append(userMessage).append("\n\n");
        promptBuilder.append("请以角色的身份回复（不要说你是AI或角色扮演，直接以角色身份回应）：");

        return promptBuilder.toString();
    }

    /**
     * 构建角色化提示词
     *
     * @param roleName        角色名称
     * @param characterPrompt 角色人格描述
     * @return 角色提示词
     */
    private String buildCharacterPrompt(String roleName, String characterPrompt) {
        StringBuilder promptBuilder = new StringBuilder();

        promptBuilder.append("你现在要扮演 ").append(roleName).append("。\n");
        if (characterPrompt != null && !characterPrompt.isEmpty()) {
            promptBuilder.append("角色设定：").append(characterPrompt).append("\n");
        }
        promptBuilder.append("请完全沉浸在这个角色中，用这个角色的语气、性格和背景来回应。");

        return promptBuilder.toString();
    }


    @Override
    public String generateCharacterResponse(String roleName, String characterPrompt, String userMessage) {
        return "";
    }

    @Override
    public void generateStreamResponse(String rolePrompt, String userMessage, StreamResponseCallback callback) {
        log.debug("开始流式生成AI回复");

        try {
            // 构建提示词
            String fullPrompt = buildRolePrompt(rolePrompt, userMessage, null);

            // 准备消息（最简单只用 user message）
            List<ChatMessage> messages = List.of(
                    UserMessage.from(fullPrompt)
            );

            // 调用流式 API
            streamingChatLanguageModel.generate(messages, new StreamingResponseHandler<AiMessage>() {
                @Override
                public void onNext(String token) {
                    callback.onResponse(token);
                }

                @Override
                public void onError(Throwable error) {
                    log.error("流式生成AI回复失败", error);
                    callback.onResponse("[ERROR]");
                }

                @Override
                public void onComplete(Response<AiMessage> response) {
                    if (response != null && response.content() != null) {
                        String finalText = response.content().text();
                        // 你可以在这里保存 finalText 到数据库
                    } else {
                        log.warn("流式回复完成，但 response 为空或无内容");
                    }
                    callback.onResponse("[DONE]");
                }
            });

        } catch (Exception e) {
            log.error("调用流式API失败", e);
            callback.onResponse("[ERROR]");
        }
    }
//    private String buildRolePrompt(String rolePrompt, String userMessage, String chatHistory) {
//        StringBuilder promptBuilder = new StringBuilder();
//
//        if (rolePrompt != null && !rolePrompt.isEmpty()) {
//            promptBuilder.append("你是一个AI角色扮演助手。请严格按照以下角色设定进行回复：\n");
//            promptBuilder.append(rolePrompt).append("\n\n");
//        }
//
//        if (chatHistory != null && !chatHistory.isEmpty()) {
//            promptBuilder.append("聊天历史：\n");
//            promptBuilder.append(chatHistory).append("\n\n");
//        }
//
//        promptBuilder.append("用户说：").append(userMessage).append("\n\n");
//        promptBuilder.append("请以角色的身份回复（不要说你是AI或角色扮演，直接以角色身份回应）：");
//
//        return promptBuilder.toString();
//    }
}