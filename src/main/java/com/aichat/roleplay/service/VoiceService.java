package com.aichat.roleplay.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/**
 * 语音服务接口
 * 通过模型API串接实现语音输入输出功能
 */
public interface VoiceService {

    /**
     * 语音对话处理 - API串接实现（带聊天和角色信息，保存消息记录）
     */
    byte[] processVoiceChat(MultipartFile audioFile, Long chatId, Long roleId);


    /**
     * 聊天室多角色语音对话处理
     * @param audioFile 用户语音文件
     * @param chatRoomId 聊天室ID
     * @return 多个角色的语音回复数据，key为角色名称，value为音频数据
     */
    Map<String, byte[]> processChatRoomMultiRoleVoiceChat(MultipartFile audioFile, Long chatRoomId);
}