package com.aichat.roleplay.service.impl;

import com.aichat.roleplay.service.IAiChatService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * AI聊天服务实现类
 * 使用LangChain4j集成OpenAI
 * 遵循SOLID原则中的单一职责原则和依赖倒置原则
 * 采用策略模式处理不同类型的AI回复生成
 */

@Service
public class AiChatServiceImpl implements IAiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatServiceImpl.class);

    private final ChatLanguageModel chatLanguageModel;

    /**
     * 构造函数注入，遵循依赖倒置原则
     *
     * @param chatLanguageModel 聊天语言模型
     */
    @Autowired
    public AiChatServiceImpl(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    @Override
    public String generateRoleResponse(String rolePrompt, String userMessage, String chatHistory) {
        log.debug("生成角色回复，角色提示: {}, 用户消息: {}", rolePrompt, userMessage);

        try {
            // 构建完整的提示词
            String fullPrompt = buildRolePrompt(rolePrompt, userMessage, chatHistory);

            // 调用AI模型生成回复
            String response = chatLanguageModel.generate(fullPrompt);

            log.debug("AI回复生成成功，长度: {}", response.length());
            return response;

        } catch (Exception e) {
            log.error("生成AI回复失败", e);
            throw new RuntimeException("AI服务暂时不可用，请稍后再试");
        }
    }

    @Override
    public String generateCharacterResponse(String roleName, String characterPrompt, String userMessage) {
        log.debug("生成角色化回复，角色: {}, 消息: {}", roleName, userMessage);

        // 使用策略模式构建不同类型的角色提示
        String rolePrompt = buildCharacterPrompt(roleName, characterPrompt);
        return generateRoleResponse(rolePrompt, userMessage, null);
    }

    @Override
    public void generateStreamResponse(String rolePrompt, String userMessage, StreamResponseCallback callback) {
        log.debug("开始流式生成AI回复");

        try {
            // 构建提示词
            String fullPrompt = buildRolePrompt(rolePrompt, userMessage, null);

            // 这里可以实现流式响应逻辑
            // 由于LangChain4j的流式API可能不同，暂时使用普通方式模拟
            String response = chatLanguageModel.generate(fullPrompt);

            // 模拟流式传输，按字符分块发送
            simulateStreamResponse(response, callback);

        } catch (Exception e) {
            log.error("流式生成AI回复失败", e);
            callback.onResponse("AI服务暂时不可用，请稍后再试");
        }
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

    /**
     * 模拟流式响应
     *
     * @param response 完整响应
     * @param callback 回调函数
     */
    private void simulateStreamResponse(String response, StreamResponseCallback callback) {
        // 简单的分块策略：按句子或标点分割
        String[] chunks = response.split("(?<=[。！？，])");

        for (String chunk : chunks) {
            if (!chunk.trim().isEmpty()) {
                callback.onResponse(chunk);

                // 模拟网络延迟
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}