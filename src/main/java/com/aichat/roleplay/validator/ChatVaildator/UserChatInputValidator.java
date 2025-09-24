package com.aichat.roleplay.validator.ChatVaildator;

import com.aichat.roleplay.validator.InputValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天输入责任链校验器，组合多种 InputValidator
 */
public class UserChatInputValidator {
    private final List<InputValidator> validators = new ArrayList<>();

    public UserChatInputValidator() {
        // 默认组合常用校验器
        validators.add(new EmptyContentValidator());
        validators.add(new LengthValidator(500));
        validators.add(new SensitiveWordValidator(List.of("敏感词1", "敏感词2")));
    }

    public void addValidator(InputValidator validator) {
        validators.add(validator);
    }

    /**
     * 校验并返回第一个不通过的错误信息，全部通过返回null
     */
    public String validateWithError(String input) {
        for (InputValidator validator : validators) {
            if (!validator.validate(input)) {
                return validator.getErrorMessage();
            }
        }
        return null;
    }

    /**
     * 校验是否全部通过
     */
    public boolean validate(String input) {
        for (InputValidator validator : validators) {
            if (!validator.validate(input)) {
                return false;
            }
        }
        return true;
    }
}
