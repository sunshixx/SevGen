package com.aichat.roleplay.controller;

import com.aichat.roleplay.service.VoiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 语音控制器 - API串接模式
 * 基于配置的语音模型进行API串接
 */
@RestController
@RequestMapping("/api/voice")
@CrossOrigin(origins = "*")
public class VoiceController {
    
    private static final Logger log = LoggerFactory.getLogger(VoiceController.class);
    
    @Autowired
    private VoiceService voiceService;
    
    /**
     * 语音对话接口 - API串接实现
     * 接收音频文件，返回音频响应
     */
    @PostMapping("/chat")
    public ResponseEntity<byte[]> voiceChat(@RequestParam("audio") MultipartFile audioFile) {
        try {
            log.info("收到语音对话请求，开始API串接处理");
            log.info("文件名: {}, 大小: {} bytes, 内容类型: {}", 
                    audioFile.getOriginalFilename(), audioFile.getSize(), audioFile.getContentType());
            
            if (audioFile.isEmpty()) {
                log.warn("音频文件为空");
                return ResponseEntity.badRequest().build();
            }

            String contentType = audioFile.getContentType();
            if (contentType != null) {
                log.info("接收到音频类型: {}", contentType);
            } else {
                log.warn("无法识别文件类型，尝试继续处理");
            }
            

            if (contentType != null && !contentType.startsWith("audio/") && !contentType.startsWith("video/")) {
                log.warn("可能不是音频文件类型: {}", contentType);
            }

            byte[] audioResponse = voiceService.processVoiceChat(audioFile);
            

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
            headers.setContentLength(audioResponse.length);
            
            log.info("API串接完成，返回音频大小: {} bytes", audioResponse.length);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(audioResponse);
                    
        } catch (Exception e) {
            log.error("API串接语音对话处理失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}