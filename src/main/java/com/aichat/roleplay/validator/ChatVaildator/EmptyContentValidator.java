package com.aichat.roleplay.validator.ChatVaildator;

import com.aichat.roleplay.validator.InputValidator;

public class EmptyContentValidator implements InputValidator {
    @Override
    public boolean validate(String input) {
        return input != null && !input.trim().isEmpty();
    }

    @Override
    public String getErrorMessage() {
        return "内容不能为空";
    }
}
