package com.aichat.roleplay.service.impl;

import com.aichat.roleplay.service.VoiceService;
import com.aichat.roleplay.service.IFileStorageService;
import com.aichat.roleplay.service.IMessageService;
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
    private RestTemplate restTemplate;
    
    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apiKey;
    
    @Value("${langchain4j.open-ai.chat-model.base-url:https://openai.qiniu.com}")
    private String baseUrl;
    
    @Override
    public byte[] processVoiceChat(MultipartFile audioFile) {
        return processVoiceChat(audioFile, null);
    }
    
    @Override
    public byte[] processVoiceChat(MultipartFile audioFile, String originalFormat) {
        try {
            long startTime = System.currentTimeMillis();
            logger.info("开始并行化语音对话处理，文件大小: " + audioFile.getSize() + " bytes");
            
            // 阶段1：并行上传和ASR处理
            CompletableFuture<String> uploadFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    long uploadStart = System.currentTimeMillis();
                    String audioUrl = fileStorageService.uploadAudioFile(audioFile, audioFile.getOriginalFilename());
                    long uploadTime = System.currentTimeMillis() - uploadStart;
                    logger.info("文件上传完成，耗时: " + uploadTime + "ms, URL: " + audioUrl);
                    return audioUrl;
                } catch (Exception e) {
                    logger.severe("文件上传失败: " + e.getMessage());
                    throw new RuntimeException("文件上传失败", e);
                }
            });

            // 阶段2：基于上传结果进行ASR处理
            CompletableFuture<String> asrFuture = uploadFuture.thenCompose(audioUrl -> 
                CompletableFuture.supplyAsync(() -> {
                    try {
                        long asrStart = System.currentTimeMillis();
                        String transcribedText = speechToTextWithModel(audioUrl, audioFile.getOriginalFilename());
                        long asrTime = System.currentTimeMillis() - asrStart;
                        logger.info("ASR处理完成，耗时: " + asrTime + "ms, 结果: " + transcribedText);
                        return transcribedText;
                    } catch (Exception e) {
                        logger.severe("ASR处理失败: " + e.getMessage());
                        throw new RuntimeException("ASR处理失败", e);
                    }
                })
            );

            // 阶段3：基于ASR结果进行AI对话
            CompletableFuture<String> aiFuture = asrFuture.thenCompose(transcribedText -> 
                CompletableFuture.supplyAsync(() -> {
                    try {
                        long aiStart = System.currentTimeMillis();
                        String aiResponse = processWithAI(transcribedText);
                        long aiTime = System.currentTimeMillis() - aiStart;
                        logger.info("AI处理完成，耗时: " + aiTime + "ms, 结果: " + aiResponse);
                        return aiResponse;
                    } catch (Exception e) {
                        logger.severe("AI处理失败: " + e.getMessage());
                        throw new RuntimeException("AI处理失败", e);
                    }
                })
            );

            // 阶段4：基于AI结果进行TTS
            CompletableFuture<byte[]> ttsFuture = aiFuture.thenCompose(aiResponse -> 
                CompletableFuture.supplyAsync(() -> {
                    try {
                        long ttsStart = System.currentTimeMillis();
                        byte[] audioResponse = textToSpeechWithModel(aiResponse);
                        long ttsTime = System.currentTimeMillis() - ttsStart;
                        logger.info("TTS处理完成，耗时: " + ttsTime + "ms, 音频大小: " + audioResponse.length + " bytes");
                        return audioResponse;
                    } catch (Exception e) {
                        logger.severe("TTS处理失败: " + e.getMessage());
                        throw new RuntimeException("TTS处理失败", e);
                    }
                })
            );

            // 等待所有异步任务完成，设置超时时间
            byte[] result = ttsFuture.get(30, TimeUnit.SECONDS);
            
            long totalTime = System.currentTimeMillis() - startTime;
            logger.info("并行化语音对话处理完成，总耗时: " + totalTime + "ms");
            
            return result;
            
        } catch (Exception e) {
            logger.severe("并行化语音对话处理失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("语音对话处理失败", e);
        }
    }

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
            
            // 阶段1：上传用户音频
            userAudioUrl = fileStorageService.uploadAudioFile(audioFile, audioFile.getOriginalFilename());
            logger.info("用户音频上传完成，URL: " + userAudioUrl);
            
            // 阶段2：语音转文字
            transcribedText = speechToTextWithModel(userAudioUrl, audioFile.getOriginalFilename());
            logger.info("语音转文字完成: " + transcribedText);
            
            // 保存用户语音消息
            messageService.saveVoiceMessage(chatId, roleId, "user", userAudioUrl, transcribedText, null);
            
            // 阶段3：AI对话
            aiResponse = processWithAI(transcribedText);
            logger.info("AI回复: " + aiResponse);
            
            // 阶段4：AI回复转语音
            byte[] aiAudioBytes = textToSpeechWithModel(aiResponse);
            logger.info("AI语音合成完成，大小: " + aiAudioBytes.length + " bytes");
            
            // 上传AI音频
            try {
                // 创建临时MultipartFile用于上传AI音频
                String aiFileName = "ai_response_" + System.currentTimeMillis() + ".mp3";
                aiAudioUrl = fileStorageService.uploadAudioFile(aiAudioBytes, aiFileName, "audio/mpeg");
                logger.info("AI音频上传完成，URL: " + aiAudioUrl);
                
                // 保存AI语音消息
                messageService.saveVoiceMessage(chatId, roleId, "ai", aiResponse, aiAudioUrl, null, null);
                
            } catch (Exception e) {
                logger.warning("AI音频上传失败: " + e.getMessage());
                // 即使上传失败，仍然返回音频数据
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
            long startTime = System.currentTimeMillis();
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
            
            // 优化：添加参数控制响应质量和速度的平衡
            requestBody.put("max_tokens", 150); // 限制回复长度，加快响应
            requestBody.put("temperature", 0.7); // 适中的创造性
            
            logger.info("发送AI请求，输入长度: " + inputText.length() + " 字符");
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            long responseTime = System.currentTimeMillis() - startTime;
            logger.info("AI响应耗时: " + responseTime + "ms");
            
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
                            String result = messageObj.get("content");
                            logger.info("AI回复长度: " + (result != null ? result.length() : 0) + " 字符");
                            return result;
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
            // 正确的API路径：TTS接口路径
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