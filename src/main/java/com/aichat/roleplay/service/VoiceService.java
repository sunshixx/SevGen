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
}