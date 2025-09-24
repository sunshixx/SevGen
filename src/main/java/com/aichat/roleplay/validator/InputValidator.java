package com.aichat.roleplay.validator;

/**
 * 顶层接口：输入校验器，适用于所有输入场景
 */
public interface InputValidator {
    /**
     * 校验输入内容
     */
    boolean validate(String input);

    /**
     * 校验失败时的错误信息
     */
    String getErrorMessage();
}
