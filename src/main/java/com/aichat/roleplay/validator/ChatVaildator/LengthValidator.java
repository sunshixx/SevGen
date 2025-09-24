package com.aichat.roleplay.validator.ChatVaildator;

import com.aichat.roleplay.validator.InputValidator;

public class LengthValidator implements InputValidator {
    private final int maxLength;

    public LengthValidator(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public boolean validate(String input) {
        return input != null && input.length() <= maxLength;
    }

    @Override
    public String getErrorMessage() {
        return "内容长度不能超过" + maxLength + "字符";
    }
}
