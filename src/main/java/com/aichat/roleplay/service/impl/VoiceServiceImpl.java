package com.aichat.roleplay.service.impl;

import com.aichat.roleplay.service.VoiceService;
import com.aichat.roleplay.service.IFileStorageService;
import com.aichat.roleplay.service.IMessageService;
import com.aichat.roleplay.service.SseService;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
public class VoiceServiceImpl implements VoiceService {

    private static final Logger logger = Logger.getLogger(VoiceServiceImpl.class.getName());
    
    @Autowired
    private IFileStorageService fileStorageService;
    
    @Autowired
    private IMessageService messageService;
    
    @Autowired
    private SseService sseService;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apiKey;
    
    @Value("${langchain4j.open-ai.chat-model.base-url:https://openai.qiniu.com}")
    private String baseUrl;


    @Override
    public byte[] processVoiceChat(MultipartFile audioFile, Long chatId, Long roleId) {
        try {
            long startTime = System.currentTimeMillis();
            logger.info("开始带消息记录的语音对话处理，聊天ID: " + chatId + ", 角色ID: " + roleId);
            
            // 声明变量用于保存中间结果
            String userAudioUrl = null;
            String transcribedText = null;
            String aiResponse = null;
            String aiAudioUrl = null;
            

            userAudioUrl = fileStorageService.uploadAudioFile(audioFile, audioFile.getOriginalFilename());
            logger.info("用户音频上传完成，URL: " + userAudioUrl);
            

            transcribedText = speechToTextWithModel(userAudioUrl, audioFile.getOriginalFilename());
            logger.info("语音转文字完成: " + transcribedText);
            

            messageService.saveVoiceMessage(chatId, roleId, "user", userAudioUrl, transcribedText, null);
            

            aiResponse = processWithAI(chatId, roleId, transcribedText);
            logger.info("AI回复: " + aiResponse);
            

            byte[] aiAudioBytes = textToSpeechWithModel(aiResponse);
            logger.info("AI语音合成完成，大小: " + aiAudioBytes.length + " bytes");
            

            try {

                String aiFileName = "ai_response_" + System.currentTimeMillis() + ".mp3";
                aiAudioUrl = fileStorageService.uploadAudioFile(aiAudioBytes, aiFileName, "audio/mpeg");
                logger.info("AI音频上传完成，URL: " + aiAudioUrl);
                
                // 保存AI语音消息（包含文本内容和音频URL）
                messageService.saveVoiceMessage(chatId, roleId, "ai", aiResponse, aiAudioUrl, null, null);
                
            } catch (Exception e) {
                logger.warning("AI音频上传失败: " + e.getMessage());
                // 即使上传失败，仍然保存AI文本消息
                messageService.saveVoiceMessage(chatId, roleId, "ai", aiResponse, null, null, null);
            }
            
            long totalTime = System.currentTimeMillis() - startTime;
            logger.info("带消息记录的语音对话处理完成，总耗时: " + totalTime + "ms");
            
            return aiAudioBytes;
            
        } catch (Exception e) {
            logger.severe("带消息记录的语音对话处理失败: " + e.getMessage());
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
            

            String format = "mp3";
            //保留拓展性，方便以后接入更多格式
//            if (filename != null) {
//                String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
//                if (ext.equals("webm") || ext.equals("wav") || ext.equals("mp3") || ext.equals("ogg")) {
//                    format = ext;
//                }
//            }
            

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
    

    private String processWithAI(Long chatId, Long roleId, String inputText) {
        try {
            logger.info("使用SSE服务处理AI对话，输入长度: " + inputText.length() + " 字符");
            long startTime = System.currentTimeMillis();


            // 调用SSE服务时禁用消息保存，避免重复保存
            String aiResponse = sseService.getAiResponseText(chatId, roleId, inputText, false);

            
            long responseTime = System.currentTimeMillis() - startTime;
            logger.info("AI响应耗时: " + responseTime + "ms，回复长度: " + 
                       (aiResponse != null ? aiResponse.length() : 0) + " 字符");
            
            return aiResponse;
            
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
            audioParams.put("voice_type", "qiniu_zh_male_hlsnkk");
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