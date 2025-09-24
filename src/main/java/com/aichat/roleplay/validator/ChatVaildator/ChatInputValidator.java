package com.aichat.roleplay.validator.ChatVaildator;

import com.aichat.roleplay.validator.InputValidator;

/**
 * 聊天输入校验器抽象类，继承 InputValidator
 * 可扩展聊天场景的通用属性和方法
 */
public abstract class ChatInputValidator implements InputValidator {
    protected String errorMessage;

    /**
     * 校验用户输入内容
     * @param input 用户输入内容
     * @return 校验通过返回true，否则false
     */
    public abstract boolean validate(String input);

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}
