package com.aichat.roleplay.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 发送验证码请求DTO
 * 用于接收发送邮箱验证码的请求
 */
public class SendVerificationCodeRequest {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    // Constructors
    public SendVerificationCodeRequest() {}

    public SendVerificationCodeRequest(String email) {
        this.email = email;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "SendVerificationCodeRequest{" +
                "email='" + email + '\'' +
                '}';
    }
}