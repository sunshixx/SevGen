package com.aichat.roleplay.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务接口
 */
public interface IFileStorageService {
    
    /**
     * 上传音频文件到服务器
     */
    String uploadAudioFile(MultipartFile audioFile, String fileName);
    
    /**
     * 上传音频文件（字节数组）到服务器
     */
    String uploadAudioFile(byte[] audioData, String fileName, String contentType);
    
    /**
     * 删除文件
     */
    boolean deleteFile(String fileName);
}