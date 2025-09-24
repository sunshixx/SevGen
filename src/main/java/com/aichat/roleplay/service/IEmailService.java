package com.aichat.roleplay.service;

/**
 * 邮件服务接口
 * 遵循SOLID原则中的接口隔离原则
 * 负责邮件发送功能
 */
public interface IEmailService {

    /**
     * 发送验证码邮件
     *
     * @param to   收件人邮箱
     * @param code 验证码
     * @return 发送是否成功
     */
    boolean sendVerificationCode(String to, String code);
}