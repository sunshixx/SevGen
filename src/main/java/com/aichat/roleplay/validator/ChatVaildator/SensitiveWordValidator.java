package com.aichat.roleplay.validator.ChatVaildator;

import com.aichat.roleplay.validator.InputValidator;

import java.util.List;

public class SensitiveWordValidator implements InputValidator {
    private final List<String> sensitiveWords;

    public SensitiveWordValidator(List<String> sensitiveWords) {
        this.sensitiveWords = sensitiveWords;
    }

    @Override
    public boolean validate(String input) {
        if (input == null) return true;
        for (String word : sensitiveWords) {
            if (input.contains(word)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getErrorMessage() {
        return "内容包含敏感词";
    }
}
