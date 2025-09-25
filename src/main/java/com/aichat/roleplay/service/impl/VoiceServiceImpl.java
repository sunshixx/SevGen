package com.aichat.roleplay.service.impl;

import com.aichat.roleplay.service.VoiceService;
import com.aichat.roleplay.service.IFileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class VoiceServiceImpl implements VoiceService {
    
    private static final Logger logger = Logger.getLogger(VoiceServiceImpl.class.getName());
    
    @Autowired
    private IFileStorageService fileStorageService;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apiKey;
    
    @Value("${langchain4j.open-ai.chat-model.base-url:https://openai.qiniu.com}")
    private String baseUrl;
    
    @Override
    public byte[] processVoiceChat(MultipartFile audioFile) {
        try {
            String audioUrl = fileStorageService.uploadAudioFile(audioFile, audioFile.getOriginalFilename());
            logger.info("音频文件上传成功，URL: " + audioUrl);

            String transcribedText = speechToTextWithModel(audioUrl, audioFile.getOriginalFilename());
            logger.info("语音转文字结果: " + transcribedText);

            String aiResponse = processWithAI(transcribedText);
            logger.info("AI回复: " + aiResponse);

            byte[] audioResponse = textToSpeechWithModel(aiResponse);
            logger.info("文字转语音完成，音频大小: " + audioResponse.length + " bytes");
            
            return audioResponse;
            
        } catch (Exception e) {
            logger.severe("语音对话处理失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("语音对话处理失败", e);
        }
    }
    
    /**
     * 使用URL方式调用七牛云ASR API
     */
    private String speechToTextWithModel(String audioUrl, String filename) {
        try {

            String url = baseUrl + "/voice/asr";
            logger.info("调用ASR API: " + url);
            logger.info("音频文件URL: " + audioUrl);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 从文件名提取格式
            String format = "mp3";
            //保留拓展性，方便以后接入更多格式
//            if (filename != null) {
//                String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
//                if (ext.equals("webm") || ext.equals("wav") || ext.equals("mp3") || ext.equals("ogg")) {
//                    format = ext;
//                }
//            }
            
            // 构建请求体 - 按照七牛云ASR API文档格式
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "asr");
            
            Map<String, Object> audioParams = new HashMap<>();
            audioParams.put("format", format);
            audioParams.put("url", audioUrl);
            requestBody.put("audio", audioParams);
            
            logger.info("请求体: " + requestBody);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            logger.info("ASR API响应状态: " + response.getStatusCode());
            logger.info("ASR API响应体: " + response.getBody());
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                if (body != null) {
                    // 根据文档，响应格式为 data.result.text
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) body.get("data");
                    if (data != null) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> result = (Map<String, Object>) data.get("result");
                        if (result != null) {
                            return (String) result.get("text");
                        }
                    }
                }
            }
            throw new RuntimeException("语音转文字API调用失败，状态码: " + response.getStatusCode());
            
        } catch (Exception e) {
            logger.severe("调用语音转文字模型失败: " + e.getMessage());
            throw new RuntimeException("语音转文字失败", e);
        }
    }
    

    private String processWithAI(String inputText) {
        try {
            String url = baseUrl + "/chat/completions";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek/deepseek-v3.1-terminus");
            
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", inputText);
            requestBody.put("messages", List.of(message));
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
                if (responseBody != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        Map<String, Object> choice = choices.get(0);
                        @SuppressWarnings("unchecked")
                        Map<String, String> messageObj = (Map<String, String>) choice.get("message");
                        if (messageObj != null) {
                            return messageObj.get("content");
                        }
                    }
                }
            }
            throw new RuntimeException("AI对话API调用失败，状态码: " + response.getStatusCode());
            
        } catch (Exception e) {
            logger.severe("AI对话处理失败: " + e.getMessage());
            throw new RuntimeException("AI对话处理失败", e);
        }
    }

    private byte[] textToSpeechWithModel(String text) {
        try {
            String url = baseUrl + "/voice/tts";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            
            Map<String, Object> audioParams = new HashMap<>();
            //todo 按照角色构建创建不同的音色
            audioParams.put("voice_type", "qiniu_zh_female_wwxkjx");
            audioParams.put("encoding", "mp3");
            audioParams.put("speed_ratio", 1.0);
            requestBody.put("audio", audioParams);
            
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("text", text);
            requestBody.put("request", requestParams);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                if (body != null) {
                    String base64Data = (String) body.get("data");
                    if (base64Data != null) {
                        return java.util.Base64.getDecoder().decode(base64Data);
                    }
                }
            }
            throw new RuntimeException("文字转语音API调用失败，状态码: " + response.getStatusCode());
            
        } catch (Exception e) {
            logger.severe("文字转语音失败: " + e.getMessage());
            throw new RuntimeException("文字转语音失败", e);
        }
    }
}