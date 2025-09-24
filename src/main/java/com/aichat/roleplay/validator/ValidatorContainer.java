package com.aichat.roleplay.validator;

import com.aichat.roleplay.validator.ChatVaildator.ChatInputValidatorChain;
import com.aichat.roleplay.validator.ChatVaildator.EmptyContentValidator;
import com.aichat.roleplay.validator.ChatVaildator.LengthValidator;
import com.aichat.roleplay.validator.ChatVaildator.SensitiveWordValidator;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 验证器容器，统一管理所有场景的责任链
 */
@Component
public class ValidatorContainer {
    private final Map<ValidationScene, ChatInputValidatorChain> sceneValidatorMap = new HashMap<>();

    @PostConstruct
    public void init() {
        // 聊天场景责任链
        ChatInputValidatorChain chatChain = new ChatInputValidatorChain();
        chatChain.addValidator(new EmptyContentValidator());
        chatChain.addValidator(new LengthValidator(500));
        chatChain.addValidator(new SensitiveWordValidator(List.of("敏感词1", "敏感词2")));
        sceneValidatorMap.put(ValidationScene.CHAT, chatChain);

        // 注册场景责任链（可自定义）
        ChatInputValidatorChain registerChain = new ChatInputValidatorChain();
        registerChain.addValidator(new EmptyContentValidator());
        registerChain.addValidator(new LengthValidator(30));
        // 可添加注册相关的特殊校验器
        sceneValidatorMap.put(ValidationScene.REGISTER, registerChain);
    }

    /**
     * 获取指定场景的责任链
     */
    public ChatInputValidatorChain getChain(ValidationScene scene) {
        return sceneValidatorMap.get(scene);
    }
}
