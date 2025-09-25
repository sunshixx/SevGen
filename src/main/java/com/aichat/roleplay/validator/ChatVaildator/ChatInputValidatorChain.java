package com.aichat.roleplay.validator.ChatVaildator;

import com.aichat.roleplay.validator.InputValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天输入责任链，支持所有实现 InputValidator 的校验器
 */
public class ChatInputValidatorChain {
    private final List<InputValidator> validators = new ArrayList<>();

    public void addValidator(InputValidator validator) {
        validators.add(validator);
    }


    public String validate(String input) {
        for (InputValidator validator : validators) {
            if (!validator.validate(input)) {
                return validator.getErrorMessage();
            }
        }
        return null;
    }

    public List<InputValidator> getValidators() {
        return validators;
    }
}
