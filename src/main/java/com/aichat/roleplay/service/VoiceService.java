package com.aichat.roleplay.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 语音服务接口
 * 通过模型API串接实现语音输入输出功能
 */
public interface VoiceService {
    
    /**
     * 语音对话处理 - API串接实现
     */
    byte[] processVoiceChat(MultipartFile audioFile);
    
    /**
     * 语音对话处理 - API串接实现（带原始格式信息）
     */
    byte[] processVoiceChat(MultipartFile audioFile, String originalFormat);

    /**
     * 语音对话处理 - API串接实现（带聊天和角色信息，保存消息记录）
     */
    byte[] processVoiceChat(MultipartFile audioFile, Long chatId, Long roleId);
}